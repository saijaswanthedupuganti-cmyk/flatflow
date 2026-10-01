package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqEaseOut
import habitiq.app.ui.theme.HqEnterDuration
import habitiq.app.ui.theme.hqReduceMotion
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqTouchTarget
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    DISCOVER("Discover", Icons.Filled.Search),
    TASKS("Manage", Icons.Filled.HomeWork),
    PROFILE("Profile", Icons.Filled.Person)
}

/**
 * App-level navigation shell. Creation actions live inside their relevant screen so the four
 * destinations stay stable and evenly spaced across phone sizes.
 */
@Composable
fun AppShell(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val c = LocalHqColors.current
    Column(modifier.fillMaxSize().background(c.background)) {
        Box(Modifier.weight(1f)) { content() }
        Box(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Surface(Modifier.fillMaxWidth(), color = c.background) {
                Row(
                    Modifier.fillMaxWidth().selectableGroup().padding(horizontal = HqSpacing.sm, vertical = HqSpacing.md),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(AppTab.HOME, selectedTab == AppTab.HOME) { onTabSelected(AppTab.HOME) }
                    BottomNavItem(AppTab.DISCOVER, selectedTab == AppTab.DISCOVER) { onTabSelected(AppTab.DISCOVER) }
                    BottomNavItem(AppTab.TASKS, selectedTab == AppTab.TASKS) { onTabSelected(AppTab.TASKS) }
                    BottomNavItem(AppTab.PROFILE, selectedTab == AppTab.PROFILE) { onTabSelected(AppTab.PROFILE) }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(tab: AppTab, selected: Boolean, onClick: () -> Unit) {
    val c = LocalHqColors.current
    val color = if (selected) c.brandPrimary else c.textTertiary
    Column(
        Modifier
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = HqSpacing.xs, vertical = HqSpacing.xs)
            .defaultMinSize(minWidth = HqTouchTarget, minHeight = HqTouchTarget),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(width = 56.dp, height = 32.dp)
                .background(if (selected) c.brandPrimarySubtle else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(HqRadius.full)),
            contentAlignment = Alignment.Center
        ) {
            Icon(tab.icon, tab.label, tint = color, modifier = Modifier.size(HqIconSize.md))
        }
        Spacer(Modifier.height(HqSpacing.xs))
        Text(tab.label, style = HqType.labelSmall, color = color, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}
