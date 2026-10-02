package habitiq.app.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.activity.ComponentActivity
import androidx.core.view.drawToBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import habitiq.app.ui.theme.HabitiqTheme
import java.io.File
import java.io.FileOutputStream
import org.junit.Rule
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders a composable to a PNG under app/build/screenshots so layouts can be inspected without a
 * device. 390 x 844 dp at xxhdpi is the benchmark phone from the design brief.
 */
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w390dp-h844dp-xxhdpi", sdk = [34])
abstract class ScreenshotHarness {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    protected fun shoot(name: String, dark: Boolean = false, fontScale: Float = 1f, content: @Composable () -> Unit) {
        compose.setContent {
            val base = LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale)) {
                HabitiqTheme(dark = dark) { Box(Modifier.fillMaxSize().background(habitiq.app.ui.theme.LocalHqColors.current.canvas)) { content() } }
            }
        }
        compose.waitForIdle()
        val bitmap = compose.activity.window.decorView.drawToBitmap()
        val dir = File("build/screenshots").apply { mkdirs() }
        FileOutputStream(File(dir, "$name.png")).use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }
}
