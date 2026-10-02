package habitiq.app.discover

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CommuteAnchorPrivacyTest {

    @Test
    fun `stored anchor is rounded to neighbourhood precision`() {
        val map = CommuteAnchor("Office", "ChIJexactBuilding", 12.971598, 77.594566).toFirestoreMap()
        assertEquals(12.97, map["lat"] as Double, 0.0)
        assertEquals(77.59, map["lng"] as Double, 0.0)
    }

    @Test
    fun `stored anchor never includes the place id`() {
        val map = CommuteAnchor("Office", "ChIJexactBuilding", 12.97, 77.59).toFirestoreMap()
        assertFalse(map.containsKey("placeId"))
    }

    @Test
    fun `legacy exact anchor is coarsened and stripped on read`() {
        val parsed = CommuteAnchor.parse(
            mapOf("label" to "College", "placeId" to "ChIJexact", "lat" to 17.385044, "lng" to 78.486671)
        )!!
        assertEquals(17.39, parsed.lat!!, 0.0)
        assertEquals(78.49, parsed.lng!!, 0.0)
        assertNull(parsed.placeId)
    }

    @Test
    fun `negative coordinates round correctly`() {
        assertEquals(-33.87, CommuteAnchor.toNeighbourhoodPrecision(-33.868820), 0.0)
    }
}
