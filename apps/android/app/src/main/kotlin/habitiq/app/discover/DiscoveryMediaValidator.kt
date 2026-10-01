package habitiq.app.discover

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

object DiscoveryMediaValidator {
    const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L // 10MB
    val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun isSupportedMimeType(mimeType: String?): Boolean {
        if (mimeType == null) return true
        return mimeType in ALLOWED_MIME_TYPES || mimeType.startsWith("image/")
    }

    fun isWithinFileSize(sizeBytes: Long?): Boolean {
        if (sizeBytes == null) return true
        return sizeBytes <= MAX_FILE_SIZE_BYTES
    }

    fun validate(contentResolver: ContentResolver?, uri: Uri): ValidationResult {
        if (contentResolver == null) return ValidationResult.Valid
        val mimeType = runCatching { contentResolver.getType(uri) }.getOrNull()
        if (!isSupportedMimeType(mimeType)) {
            return ValidationResult.Invalid("Only JPEG, PNG, or WebP images are supported.")
        }
        var size: Long? = null
        try {
            contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        if (size == null) {
            try {
                contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    size = pfd.statSize
                }
            } catch (_: Exception) {}
        }

        if (!isWithinFileSize(size)) {
            return ValidationResult.Invalid("Images must be under 10MB each.")
        }
        return ValidationResult.Valid
    }
}
