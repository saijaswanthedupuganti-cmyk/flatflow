package habitiq.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiscoveryRepositoryTest {
    @Test
    fun `connection id is identical for either participant direction`() {
        assertEquals(
            connectionDocumentId("alice", "bob", "flat-1"),
            connectionDocumentId("bob", "alice", "flat-1")
        )
    }

    @Test
    fun `connection id keeps separate discovery contexts separate`() {
        assertNotEquals(
            connectionDocumentId("alice", "bob", "flat-1"),
            connectionDocumentId("alice", "bob", "seeker-bob")
        )
    }

    @Test
    fun `current vacancy projection retains photos and explicit room fields`() {
        val listing = parseVacancyListingDocument(
            "flat-1",
            mapOf(
                "name" to "Lake View",
                "flatType" to "apartment",
                "vacancy" to mapOf(
                    "active" to true,
                    "roomType" to "shared",
                    "bedsAvailable" to 1L,
                    "photoUrls" to listOf("https://example.test/one.jpg", "https://example.test/two.jpg"),
                    "amenities" to listOf("WiFi", "AC"),
                    "securityDeposit" to 24000.50,
                    "availableFrom" to "2026-10-01",
                    "approximateLocationOnly" to true
                )
            )
        )!!

        assertEquals("shared", listing.roomType)
        assertEquals(1, listing.bedsAvailable)
        assertEquals(2, listing.photoUrls.size)
        assertEquals(listOf("WiFi", "AC"), listing.amenities)
        assertEquals(24000.50, listing.securityDeposit!!, 0.0)
        assertEquals("2026-10-01", listing.availableFrom)
    }

    @Test
    fun `legacy vacancy does not infer room type from bed count`() {
        val listing = parseVacancyListingDocument(
            "legacy-flat",
            mapOf("vacancy" to mapOf("active" to true, "bedsAvailable" to 1L))
        )!!

        assertNull(listing.roomType)
    }
}
