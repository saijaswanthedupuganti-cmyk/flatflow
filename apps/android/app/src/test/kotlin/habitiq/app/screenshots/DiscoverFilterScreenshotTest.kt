package habitiq.app.screenshots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.data.VacancyListing
import habitiq.app.discover.DiscoverMode
import habitiq.app.discover.SeekerFilters
import habitiq.app.discover.VacancyFilters
import habitiq.app.ui.components.HqSegmentedControl
import habitiq.app.ui.discover.DiscoverFilterPanel
import habitiq.app.ui.discover.UseAFlatDiscoverContent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Search & filters: the filter sheet content and the results list with applied filters. */
@RunWith(RobolectricTestRunner::class)
class DiscoverFilterScreenshotTest : ScreenshotHarness() {
    private val applied = VacancyFilters(rentMin = "8000", rentMax = "12000", roomType = "private", genderPreference = "female", available = "now")

    @androidx.compose.runtime.Composable
    private fun panel(filters: VacancyFilters) = DiscoverFilterPanel(
        DiscoverMode.USE_A_FLAT, filters, SeekerFilters(), {}, {}, {}, 12, onClose = {}, onShow = {},
    )

    @Test fun filters() = shoot("discover-filters") { panel(VacancyFilters()) }
    @Test fun filtersApplied() = shoot("discover-filters-applied") { panel(applied) }
    @Config(qualifiers = "w390dp-h1600dp-xxhdpi", sdk = [34])
    @Test fun filtersMore() = shoot("discover-filters-more") { panel(applied.copy(furnishing = "furnished", lifestyleTags = setOf("Vegetarian"))) }
    @Config(qualifiers = "w390dp-h1800dp-xxhdpi", sdk = [34])
    @Test fun filtersLarge() = shoot("discover-filters-font2", fontScale = 2f) { panel(applied) }
    @Config(qualifiers = "w390dp-h1600dp-xxhdpi", sdk = [34])
    @Test fun resultsLarge() = shoot("discover-results-font2", fontScale = 2f) {
        UseAFlatDiscoverContent(flats, flats, applied, {}, false, null, {}, {}, {}, savedIds = emptySet(), onToggleSave = {})
    }

    @Test fun people() = shoot("discover-filters-people") {
        DiscoverFilterPanel(DiscoverMode.FIND_A_PERSON, VacancyFilters(), SeekerFilters(gender = "female"), {}, {}, {}, 3, onClose = {}, onShow = {})
    }

    private val flats = listOf(
        VacancyListing(
            flatId = "a", flatName = "Sunrise Heights 4B", active = true, city = "Hyderabad", area = "Gachibowli",
            rentPerHead = 9500.0, currency = "INR", bedsAvailable = 1, preferredGender = "female", about = "",
            memberCount = 2, roomType = "private", furnishing = "furnished", availableFrom = "2020-01-01",
            lifestyle = listOf("Vegetarian", "Quiet"), amenities = listOf("WiFi", "AC", "Geyser"),
        ),
    )

    @Test fun resultsWithFilters() = shoot("discover-results-filtered") {
        Column {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { HqSegmentedControl(listOf("Find a flat", "Find a flatmate"), 0, {}) }
            UseAFlatDiscoverContent(flats, flats, applied.copy(cityArea = "Gachibowli"), {}, false, null, {}, {}, {}, onNearPlace = {}, savedIds = setOf("a"), onToggleSave = {})
        }
    }
}
