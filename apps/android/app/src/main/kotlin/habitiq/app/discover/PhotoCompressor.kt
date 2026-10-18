package habitiq.app.discover

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Shrinks listing photos on the phone before upload. Camera photos are often 5–15 MB, which is slow on
 * mobile data, times out, and can trip the 10 MB storage rule. A 1600 px JPEG at quality 82 looks the same
 * on a phone screen and is usually 200–500 KB.
 */
object PhotoCompressor {
    const val MAX_EDGE = 1600
    const val QUALITY = 82

    /** The size to scale a [width] x [height] image to so its longest edge is at most [maxEdge]. Never upscales. */
    fun targetSize(width: Int, height: Int, maxEdge: Int = MAX_EDGE): Pair<Int, Int> {
        val longest = max(width, height)
        if (longest <= maxEdge || longest <= 0) return width to height
        val scale = maxEdge.toDouble() / longest
        return (width * scale).roundToInt().coerceAtLeast(1) to (height * scale).roundToInt().coerceAtLeast(1)
    }

    /** Largest power-of-two decode step that still leaves the image at least [maxEdge] on its longest side. */
    fun sampleSize(width: Int, height: Int, maxEdge: Int = MAX_EDGE): Int {
        var sample = 1
        while (max(width, height) / (sample * 2) >= maxEdge) sample *= 2
        return sample
    }

    /** JPEG bytes ready to upload, upright (camera rotation applied). Throws if the image can't be read. */
    fun compress(resolver: ContentResolver, uri: Uri): ByteArray {
        val bitmap = if (Build.VERSION.SDK_INT >= 28) decodeModern(resolver, uri) else decodeLegacy(resolver, uri)
        try {
            val out = ByteArrayOutputStream()
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)) { "Couldn't encode the photo" }
            return out.toByteArray()
        } finally {
            bitmap.recycle()
        }
    }

    // ImageDecoder applies EXIF rotation itself and can scale while decoding.
    @androidx.annotation.RequiresApi(28)
    private fun decodeModern(resolver: ContentResolver, uri: Uri): Bitmap =
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
            val (w, h) = targetSize(info.size.width, info.size.height)
            decoder.setTargetSize(w, h)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE // hardware bitmaps can't be compressed
        }

    private fun decodeLegacy(resolver: ContentResolver, uri: Uri): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight) }
        val raw = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Couldn't read the photo")
        val (w, h) = targetSize(raw.width, raw.height)
        val scaled = if (w != raw.width || h != raw.height) Bitmap.createScaledBitmap(raw, w, h, true).also { if (it !== raw) raw.recycle() } else raw
        val degrees = runCatching {
            resolver.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            }
        }.getOrNull() ?: 0f
        if (degrees == 0f) return scaled
        val rotated = Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, Matrix().apply { postRotate(degrees) }, true)
        if (rotated !== scaled) scaled.recycle()
        return rotated
    }
}
