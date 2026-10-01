package habitiq.app.discover

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoveryMediaValidatorTest {

    @Test
    fun `allowed mime types contains standard web images`() {
        assertTrue(DiscoveryMediaValidator.ALLOWED_MIME_TYPES.contains("image/jpeg"))
        assertTrue(DiscoveryMediaValidator.ALLOWED_MIME_TYPES.contains("image/png"))
        assertTrue(DiscoveryMediaValidator.ALLOWED_MIME_TYPES.contains("image/webp"))
    }

    @Test
    fun `max file size is 10MB`() {
        assertEquals(10 * 1024 * 1024L, DiscoveryMediaValidator.MAX_FILE_SIZE_BYTES)
    }

    @Test
    fun `isSupportedMimeType checks correctly`() {
        assertTrue(DiscoveryMediaValidator.isSupportedMimeType("image/jpeg"))
        assertTrue(DiscoveryMediaValidator.isSupportedMimeType("image/png"))
        assertTrue(DiscoveryMediaValidator.isSupportedMimeType("image/webp"))
        assertTrue(DiscoveryMediaValidator.isSupportedMimeType("image/heic"))
        assertTrue(DiscoveryMediaValidator.isSupportedMimeType(null))
        assertFalse(DiscoveryMediaValidator.isSupportedMimeType("application/pdf"))
        assertFalse(DiscoveryMediaValidator.isSupportedMimeType("video/mp4"))
    }

    @Test
    fun `isWithinFileSize enforces 10MB limit`() {
        assertTrue(DiscoveryMediaValidator.isWithinFileSize(null))
        assertTrue(DiscoveryMediaValidator.isWithinFileSize(5 * 1024 * 1024L))
        assertTrue(DiscoveryMediaValidator.isWithinFileSize(10 * 1024 * 1024L))
        assertFalse(DiscoveryMediaValidator.isWithinFileSize(10 * 1024 * 1024L + 1))
    }
}
