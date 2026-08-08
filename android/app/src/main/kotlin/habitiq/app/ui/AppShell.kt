package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.FigmaColors

enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    DISCOVER("Discover", Icons.Filled.Search),
    TASKS("Tasks", Icons.Filled.ListAlt),
    PROFILE("Profile", Icons.Filled.Person)
}

@Composable
fun AppShell(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onPlusClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier.fillMaxSize().background(FigmaColors.Background)) {
        Box(Modifier.weight(1f)) { content() }
        Box(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Surface(Modifier.fillMaxWidth(), color = FigmaColors.Background, shadowElevation = 8.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(AppTab.HOME, selectedTab == AppTab.HOME) { onTabSelected(AppTab.HOME) }
                    BottomNavItem(AppTab.DISCOVER, selectedTab == AppTab.DISCOVER) { onTabSelected(AppTab.DISCOVER) }
                    Spacer(Modifier.width(56.dp))
                    BottomNavItem(AppTab.TASKS, selectedTab == AppTab.TASKS) { onTabSelected(AppTab.TASKS) }
                    BottomNavItem(AppTab.PROFILE, selectedTab == AppTab.PROFILE) { onTabSelected(AppTab.PROFILE) }
                }
            }
            Box(
                Modifier.align(Alignment.TopCenter).offset(y = (-20).dp).size(56.dp)
                    .shadow(12.dp, CircleShape, spotColor = FigmaColors.Primary.copy(0.4f))
                    .background(FigmaColors.Primary, CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .clickable(onClick = onPlusClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, "Add", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun BottomNavItem(tab: AppTab, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) FigmaColors.Primary else FigmaColors.InkMuted
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(tab.icon, tab.label, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(tab.label, color = color, fontSize = 10.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}
