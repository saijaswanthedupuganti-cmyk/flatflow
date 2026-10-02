package habitiq.app.screenshots

import habitiq.app.ui.IntentChooserScreen
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardingScreenshotTest : ScreenshotHarness() {
    @Test fun normal() = shoot("onboarding-light") { IntentChooserScreen("Sai", {}, {}) }
    @Test fun dark() = shoot("onboarding-dark", dark = true) { IntentChooserScreen("Sai", {}, {}) }
    @Test fun large() = shoot("onboarding-200", fontScale = 2f) { IntentChooserScreen("Sai", {}, {}) }
}
