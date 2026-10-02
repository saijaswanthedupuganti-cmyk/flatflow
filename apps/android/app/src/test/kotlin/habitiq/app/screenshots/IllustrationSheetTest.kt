package habitiq.app.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqArt
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.theme.LocalHqColors
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalLayoutApi::class)
@RunWith(RobolectricTestRunner::class)
class IllustrationSheetTest : ScreenshotHarness() {
    private fun sheet(name: String, dark: Boolean) = shoot(name, dark = dark) {
        val c = LocalHqColors.current
        Column(Modifier.fillMaxSize().background(c.canvas).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HqArt.entries.forEach { art ->
                    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(c.selectedBg).padding(12.dp)) {
                        HqIllustration(art, Modifier.width(if (art.aspect > 1f) 150.dp else 104.dp))
                    }
                }
            }
            // the real display size used on Home (96dp) for a small-scale readability check
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(c.selectedBg).padding(12.dp)) {
                HqIllustration(HqArt.Home, Modifier.size(80.dp))
            }
        }
    }

    @Test fun light() = sheet("illustrations-light", dark = false)
    @Test fun dark() = sheet("illustrations-dark", dark = true)
}
