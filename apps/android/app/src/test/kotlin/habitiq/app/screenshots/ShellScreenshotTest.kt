package habitiq.app.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.ui.AppShell
import habitiq.app.ui.AppTab
import habitiq.app.ui.ShellCreate
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShellScreenshotTest : ScreenshotHarness() {
    private fun sheet(name: String, dark: Boolean) = shoot(name, dark = dark) {
        val c = LocalHqColors.current
        Column(Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(
                "Home" to (AppTab.HOME to ShellCreate.None),
                "Discover" to (AppTab.DISCOVER to ShellCreate.Direct("Create a Discovery post") {}),
                "Manage" to (AppTab.TASKS to ShellCreate.Direct("Add task") {}),
                "Profile" to (AppTab.PROFILE to ShellCreate.None),
            ).forEach { (label, pair) ->
                Text(label, style = HqType.labelSmall, color = c.textSecondary, modifier = Modifier.padding(horizontal = 20.dp))
                Box(Modifier.fillMaxWidth().height(140.dp)) {
                    AppShell(selectedTab = pair.first, onTabSelected = {}, createAction = pair.second) { Box(Modifier.background(c.canvas)) }
                }
            }
        }
    }

    @Test fun light() = sheet("shell-light", false)
    @Test fun dark() = sheet("shell-dark", true)
}
