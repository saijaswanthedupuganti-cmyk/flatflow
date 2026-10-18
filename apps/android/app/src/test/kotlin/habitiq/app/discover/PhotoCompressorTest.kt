package habitiq.app.discover

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoCompressorTest {
    @Test fun `big camera photos shrink to the max edge keeping their shape`() {
        assertEquals(1600 to 1200, PhotoCompressor.targetSize(4000, 3000))
        assertEquals(900 to 1600, PhotoCompressor.targetSize(3024, 5376))
    }

    @Test fun `small photos are never upscaled`() {
        assertEquals(1200 to 800, PhotoCompressor.targetSize(1200, 800))
        assertEquals(1600 to 1600, PhotoCompressor.targetSize(1600, 1600))
    }

    @Test fun `decode step keeps enough pixels for the target`() {
        assertEquals(2, PhotoCompressor.sampleSize(4000, 3000)) // 2000 px >= 1600, 1000 px would be too small
        assertEquals(4, PhotoCompressor.sampleSize(8000, 6000))
        assertEquals(1, PhotoCompressor.sampleSize(1600, 1200))
    }
}
