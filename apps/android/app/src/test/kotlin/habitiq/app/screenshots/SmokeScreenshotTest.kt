package habitiq.app.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SmokeScreenshotTest : ScreenshotHarness() {
    @Test fun renders() {
        shoot("smoke-light") {
            val c = LocalHqColors.current
            Column(Modifier.fillMaxSize().background(c.canvas).padding(20.dp)) {
                Text("Smoke test", style = HqType.display, color = c.textPrimary)
                HqButton(text = "Primary", onClick = {})
            }
        }
    }
}
