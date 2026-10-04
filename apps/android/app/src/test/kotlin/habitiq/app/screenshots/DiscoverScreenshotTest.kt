package habitiq.app.screenshots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.data.SeekerProfile
import habitiq.app.data.VacancyListing
import habitiq.app.discover.SeekerFilters
import habitiq.app.discover.VacancyFilters
import habitiq.app.ui.components.HqSegmentedControl
import habitiq.app.ui.discover.FindFlatmateDiscoverContent
import habitiq.app.ui.discover.UseAFlatDiscoverContent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DiscoverScreenshotTest : ScreenshotHarness() {
    private val flats = listOf(
        VacancyListing(
            flatId = "a", flatName = "Sunrise Heights 4B", active = true, city = "Hyderabad", area = "Gachibowli",
            rentPerHead = 8500.0, currency = "INR", bedsAvailable = 1, preferredGender = "any",
            about = "Bright 3BHK near the metro with a balcony. Quiet weekdays, friendly weekends.",
            flatType = "apartment", memberCount = 2, roomType = "private",
            lifestyle = listOf("Quiet evenings", "Vegetarian", "No smoking", "Early bird", "Pet friendly"),
            customTags = listOf("Near metro", "Gym nearby", "Balcony"),
        ),
        VacancyListing(
            flatId = "b", flatName = "Green Park Residency with a very long flat name", active = true, city = "Hyderabad", area = "Kondapur",
            rentPerHead = 6200.0, currency = "INR", bedsAvailable = 2, preferredGender = "female",
            about = "", roomType = "shared", memberCount = 0, lifestyle = listOf("Social"),
        ),
    )
    private val people = listOf(
        SeekerProfile(id = "1", displayName = "Meera Nair", city = "Hyderabad", lookingIn = "Madhapur", budget = 9000.0,
            bio = "Product designer, early riser, cooks on weekends. Looking for a calm, tidy flat.", lifestyleTags = "Vegetarian, No smoking"),
        SeekerProfile(id = "2", displayName = "Rahul", city = "Hyderabad", lookingIn = "", budget = 0.0),
    )
    private val trust = habitiq.app.discover.TrustCopy.forSeeker(true)

    @Test fun flats() = shoot("discover-flats") {
        Column {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { HqSegmentedControl(listOf("Find a flat", "Find a person"), 0, {}) }
            UseAFlatDiscoverContent(flats, flats, VacancyFilters(cityArea = "Hyderabad", roomType = "private"), {}, false, null, {}, {}, {})
        }
    }

    private fun request(status: String) = habitiq.app.data.VacancyRequest(
        requesterUid = "m1", requesterName = "Meera",
        vacancy = habitiq.app.data.VacancyData(active = false, city = "Hyderabad", area = "Gachibowli", rentPerHead = 8500.0),
        status = status,
    )

    @Test fun myPostsPendingRequest() = shoot("discover-my-posts-pending") { myPosts(request("pending")) }

    @Test fun myPostsDeclinedRequest() = shoot("discover-my-posts-declined") { myPosts(request("declined")) }

    @androidx.compose.runtime.Composable
    private fun myPosts(r: habitiq.app.data.VacancyRequest) = habitiq.app.ui.discover.MyPostsScreen(
        isAdmin = false, pendingRequest = r, flat = null, vacancy = null, mySeeker = null, incomingCount = 0,
        onPauseVacancy = {}, onResumeVacancy = {}, onCloseVacancy = {}, onPauseLooking = {}, onResumeLooking = {},
        onEdit = {}, onViewConnections = {}, onBack = {},
    )

    @Test fun people() = shoot("discover-people") {
        Column {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { HqSegmentedControl(listOf("Find a flat", "Find a person"), 1, {}) }
            FindFlatmateDiscoverContent(people, people, SeekerFilters(gender = "female"), {}, false, null, {}, null, true, {}, {}, {})
        }
    }
}
