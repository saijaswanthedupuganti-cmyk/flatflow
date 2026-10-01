package habitiq.app.discover

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiscoveryDraftModelTest {

    @Test
    fun `default vacancy draft has initial values`() {
        val draft = VacancyDraft()
        assertEquals(0, draft.step)
        assertEquals("", draft.city)
        assertEquals("1", draft.beds)
        assertEquals("any", draft.gender)
        assertEquals("private", draft.roomType)
        assertEquals("furnished", draft.furnishing)
        assertEquals(emptyList<String>(), draft.tags)
        assertEquals(emptyList<String>(), draft.selectedPhotoUris)
        assertNull(draft.coverPhotoUri)
    }

    @Test
    fun `custom vacancy draft maintains state`() {
        val draft = VacancyDraft(
            step = 3,
            city = "Hyderabad",
            area = "Hitech City",
            rent = "15000",
            beds = "2",
            gender = "male",
            tags = listOf("chill", "clean"),
            roomType = "private",
            selectedPhotoUris = listOf("uri1", "uri2"),
            coverPhotoUri = "uri1"
        )
        assertEquals(3, draft.step)
        assertEquals("Hyderabad", draft.city)
        assertEquals("Hitech City", draft.area)
        assertEquals("15000", draft.rent)
        assertEquals("2", draft.beds)
        assertEquals(listOf("chill", "clean"), draft.tags)
        assertEquals("uri1", draft.coverPhotoUri)
    }

    @Test
    fun `looking draft maintains seeker state`() {
        val draft = LookingDraft(
            city = "Bengaluru",
            lookingIn = "Indiranagar, Koramangala",
            budget = "20000",
            bio = "Software engineer looking for quiet flat",
            gender = "female",
            tags = listOf("cooks", "vegetarian")
        )
        assertEquals("Bengaluru", draft.city)
        assertEquals("Indiranagar, Koramangala", draft.lookingIn)
        assertEquals("20000", draft.budget)
        assertEquals(listOf("cooks", "vegetarian"), draft.tags)
    }
}
