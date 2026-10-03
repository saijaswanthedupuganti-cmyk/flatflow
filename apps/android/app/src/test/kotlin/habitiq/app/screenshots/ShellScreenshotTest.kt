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
                Triple("Home", AppTab.HOME, ShellCreate.None),
                Triple("Discover", AppTab.DISCOVER, ShellCreate.Direct("Create a Discovery post") {}),
                Triple("Manage", AppTab.TASKS, ShellCreate.Direct("Add task") {}),
                Triple("Profile", AppTab.PROFILE, ShellCreate.None),
                Triple("Home + mic", AppTab.HOME, ShellCreate.Menu(listOf(habitiq.app.ui.CreateOption("Add expense") {}))),
            ).forEach { (label, tab, create) ->
                Text(label, style = HqType.labelSmall, color = c.textSecondary, modifier = Modifier.padding(horizontal = 20.dp))
                Box(Modifier.fillMaxWidth().height(140.dp)) {
                    AppShell(
                        selectedTab = tab, onTabSelected = {}, createAction = create,
                        onMic = if (label.endsWith("mic")) ({}) else null,
                    ) { Box(Modifier.background(c.canvas)) }
                }
            }
        }
    }

    private fun mic(name: String, dark: Boolean) = shoot(name, dark = dark) {
        val c = LocalHqColors.current
        Box(Modifier.fillMaxWidth().height(320.dp).padding(top = 24.dp)) {
            AppShell(
                selectedTab = AppTab.HOME, onTabSelected = {},
                createAction = ShellCreate.Menu(listOf(habitiq.app.ui.CreateOption("Add expense") {})), onMic = {},
            ) { Box(Modifier.background(c.canvas)) }
        }
    }

    @Test fun micLight() = mic("shell-mic-light", false)
    @Test fun micDark() = mic("shell-mic-dark", true)
    @Test fun light() = sheet("shell-light", false)
    @Test fun dark() = sheet("shell-dark", true)
}
