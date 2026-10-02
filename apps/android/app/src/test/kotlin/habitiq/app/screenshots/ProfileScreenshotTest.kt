package habitiq.app.screenshots

import androidx.compose.foundation.layout.Column
import habitiq.app.ui.ProfileHomeContent
import habitiq.app.ui.ProfileUiModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProfileScreenshotTest : ScreenshotHarness() {
    private val model = ProfileUiModel(
        displayName = "Sai Jaswanth", email = "demo@stepsai.ca",
        flatName = "Sunrise Heights 4B", flatSupport = "Admin · 4 members · switch flats",
        discoverySupport = "Your looking post is visible",
    )

    private fun render(name: String, m: ProfileUiModel, dark: Boolean = false, font: Float = 1f) = shoot(name, dark = dark, fontScale = font) {
        Column { ProfileHomeContent(m, {}, {}, {}, {}, {}, {}) }
    }

    @Test fun normal() = render("profile-light", model)
    @Test fun dark() = render("profile-dark", model, dark = true)
    @Test fun noFlatLongEmail() = render("profile-noflat-200", model.copy(flatName = null, email = "a.very.long.email.address.for.testing@example-company.co.in"), font = 2f)
}
