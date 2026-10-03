package habitiq.app.screenshots

import habitiq.app.data.VacancyListing
import habitiq.app.discover.ConnectionStatus
import habitiq.app.discover.FlatHealthSnapshot
import habitiq.app.discover.PublicMemberSummary
import habitiq.app.discover.TrustPresentation
import habitiq.app.discover.TrustTier
import habitiq.app.ui.discover.FlatListingDetailScreen
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ListingDetailScreenshotTest : ScreenshotHarness() {
    private val listing = VacancyListing(
        flatId = "f1", flatName = "Asvasidh Abode", active = true, city = "Hyderabad", area = "J.V. colony",
        rentPerHead = 5000.0, currency = "INR", bedsAvailable = 5, preferredGender = "female", about = "",
        flatType = "apartment", memberCount = 1, roomType = "shared", furnishing = "semi_furnished",
        lifestyle = listOf("No Smoking", "No Alcohol", "Work from Home", "Quiet"),
        amenities = listOf("WiFi", "AC", "Attached Bathroom", "Washing Machine", "Geyser", "Gym"),
        customTags = listOf("Women only preferred", "Working professional", "Immediate"),
        health = FlatHealthSnapshot.empty().copy(
            headline = "New household",
            members = listOf(PublicMemberSummary("Pranav Tamada", "admin")),
        ),
    )

    private fun render(name: String, dark: Boolean) = shoot(name, dark = dark) {
        FlatListingDetailScreen(
            listing = listing,
            trust = TrustPresentation(TrustTier.HABITIQ_MEMBER, "Oddroof member", "Members use Oddroof together."),
            signals = emptyList(),
            connectionStatus = ConnectionStatus.NONE,
            incomingRequestId = null, blocked = false, isOwnListing = false,
            interestedCount = 0, interestedPeople = emptyList(), onOpenInterested = {},
            onBack = {}, onConnect = {}, onMessage = {}, onAccept = {}, onDecline = {}, onReport = {}, onBlock = {},
            saved = false, onToggleSave = {},
        )
    }

    @Config(qualifiers = "w390dp-h1800dp-xxhdpi", sdk = [34])
    @Test fun light() = render("listing-light", false)

    @Config(qualifiers = "w390dp-h1800dp-xxhdpi", sdk = [34])
    @Test fun dark() = render("listing-dark", true)
}
