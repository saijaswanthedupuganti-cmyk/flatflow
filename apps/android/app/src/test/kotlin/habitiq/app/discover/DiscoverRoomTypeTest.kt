package habitiq.app.discover

import habitiq.app.data.VacancyListing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoverRoomTypeTest {
    private fun listing(id: String, roomType: String?, beds: Int) = VacancyListing(
        flatId = id,
        flatName = id,
        active = true,
        city = "Hyderabad",
        area = "Gachibowli",
        rentPerHead = 12000.0,
        currency = "INR",
        bedsAvailable = beds,
        preferredGender = "any",
        about = "",
        roomType = roomType
    )

    @Test
    fun `room filter uses explicit type rather than beds`() {
        val records = listOf(
            listing("explicit-shared-one-bed", "shared", 1),
            listing("explicit-private-three-beds", "private", 3),
            listing("legacy-one-bed", null, 1)
        )

        val privateResults = DiscoverFilterLogic.applyVacancyFilters(records, VacancyFilters(roomType = "private"))
        val sharedResults = DiscoverFilterLogic.applyVacancyFilters(records, VacancyFilters(roomType = "shared"))

        assertEquals(listOf("explicit-private-three-beds"), privateResults.map { it.flatId })
        assertEquals(listOf("explicit-shared-one-bed"), sharedResults.map { it.flatId })
        assertTrue(DiscoverFilterLogic.applyVacancyFilters(records, VacancyFilters()).containsAll(records))
    }

    @Test
    fun `legacy room label is honest`() {
        assertEquals("Room type not specified", DiscoverFilterLogic.formatRoomType(null, 1))
    }
}
