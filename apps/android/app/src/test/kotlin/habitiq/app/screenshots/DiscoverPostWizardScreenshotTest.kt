package habitiq.app.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.core.view.drawToBitmap
import habitiq.app.data.VacancyData
import habitiq.app.flats.FlatInfo
import habitiq.app.ui.discover.CreateDiscoveryPostScreen
import habitiq.app.ui.theme.HabitiqTheme
import habitiq.app.ui.theme.LocalHqColors
import java.io.File
import java.io.FileOutputStream
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** "List a room" (4 steps) and "I'm looking for a room" (3 steps), light and at 200% font. */
@RunWith(RobolectricTestRunner::class)
class DiscoverPostWizardScreenshotTest : ScreenshotHarness() {
    private val flat = FlatInfo(id = "f1", name = "Sunrise Heights 4B", adminUid = "a", memberCount = 2)
    private val vacancy = VacancyData(
        active = true, city = "Hyderabad", area = "Gachibowli", rentPerHead = 8500.0, bedsAvailable = 1,
        preferredGender = "women_only", roomType = "private", furnishing = "semi_furnished", securityDeposit = 25000.0,
        noticePeriod = "1_month", amenities = listOf("WiFi", "AC"), lifestyle = listOf("Vegetarian", "Quiet"),
        about = "Bright room with a balcony, 5 minutes to the metro.",
    )

    @Composable
    private fun room(step: Int, isAdmin: Boolean = true, existing: VacancyData? = vacancy) = CreateDiscoveryPostScreen(
        isAdmin = isAdmin, flat = flat, existingVacancy = existing,
        onPublishVacancy = { _, _ -> }, onPublishLooking = { _, _, _, _, _, _ -> }, onBack = {},
        initialType = "VACANCY", initialStep = step,
    )

    @Composable
    private fun looking(step: Int) = CreateDiscoveryPostScreen(
        isAdmin = false, flat = null, existingVacancy = null,
        onPublishVacancy = { _, _ -> }, onPublishLooking = { _, _, _, _, _, _ -> }, onBack = {},
        initialType = "LOOKING", initialStep = step,
    )

    @Test fun room1() = shoot("post-room-1") { room(0) }
    @Test fun room2() = shoot("post-room-2") { room(1) }
    @Test fun room3() = shoot("post-room-3") { room(2) }
    @Config(qualifiers = "w390dp-h1700dp-xxhdpi", sdk = [34])
    @Test fun room4() = shoot("post-room-4") { room(3, isAdmin = false) }

    @Test fun room1Large() = shoot("post-room-1-font2", fontScale = 2f) { room(0) }
    @Test fun room2Large() = shoot("post-room-2-font2", fontScale = 2f) { room(1) }
    @Config(qualifiers = "w390dp-h1400dp-xxhdpi", sdk = [34])
    @Test fun room3Large() = shoot("post-room-3-font2", fontScale = 2f) { room(2) }
    @Config(qualifiers = "w390dp-h2600dp-xxhdpi", sdk = [34])
    @Test fun room4Large() = shoot("post-room-4-font2", fontScale = 2f) { room(3, isAdmin = false) }

    /** Continue on an empty rent step shows the inline error next to the field. */
    @Test fun roomRentError() = shootAfter("post-room-2-error", { room(1, existing = null) }) {
        compose.onNodeWithText("Continue").performClick()
    }

    @Test fun looking1() = shoot("post-looking-1") { looking(0) }
    @Test fun looking2() = shoot("post-looking-2") { looking(1) }
    @Test fun looking3() = shoot("post-looking-3") { looking(2) }
    @Config(qualifiers = "w390dp-h1400dp-xxhdpi", sdk = [34])
    @Test fun looking2Large() = shoot("post-looking-2-font2", fontScale = 2f) { looking(1) }

    private fun shootAfter(name: String, content: @Composable () -> Unit, action: () -> Unit) {
        compose.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(base.density, 1f)) {
                HabitiqTheme { Box(Modifier.fillMaxSize().background(LocalHqColors.current.canvas)) { content() } }
            }
        }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(1_000)
        action()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        val bitmap = compose.activity.window.decorView.drawToBitmap()
        val dir = File("build/screenshots").apply { mkdirs() }
        FileOutputStream(File(dir, "$name.png")).use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }
}
