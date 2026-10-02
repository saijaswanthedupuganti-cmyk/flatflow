package habitiq.app.ui

import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqMenuRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqNavRow
import habitiq.app.ui.components.HqRowDivider
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", HqIcons.Home),
    DISCOVER("Discover", HqIcons.Discover),
    TASKS("Manage", HqIcons.Manage),
    PROFILE("Profile", HqIcons.Profile)
}

/** One entry in the create menu. */
data class CreateOption(val label: String, val onClick: () -> Unit)

/**
 * The contextual create action in the shell's centre slot. It is an action, never a destination:
 * [None] leaves the slot empty so the four destinations stay put, [Direct] performs one authorised
 * action, and [Menu] lets the person pick among the actions they may take here.
 */
sealed interface ShellCreate {
    data object None : ShellCreate
    data class Direct(val label: String, val onClick: () -> Unit) : ShellCreate
    data class Menu(val options: List<CreateOption>) : ShellCreate
}

/**
 * App-level navigation shell: Home, Manage, [contextual +], Discover, Profile (Figma order). The four
 * destinations are unchanged from the previous app; the centre slot is only for creating things.
 *
 * Follows `navigation.*` from the design doc: `surface.base`, an 80dp content area with the system
 * inset added once underneath, every destination at least 48dp wide, and the bar hidden while the
 * keyboard is open.
 */
@Composable
fun AppShell(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
    createAction: ShellCreate = ShellCreate.None,
    content: @Composable () -> Unit
) {
    val c = LocalHqColors.current
    var menuOpen by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(c.canvas)) {
        Box(Modifier.weight(1f)) { content() }
        val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        if (!keyboardOpen) ShellBar(selectedTab, onTabSelected, createAction) { menuOpen = true }
    }
    if (menuOpen && createAction is ShellCreate.Menu) {
        CreateMenuSheet(createAction.options) { menuOpen = false }
    }
}

/**
 * Floating pill from the Figma Make file: inset from the screen edges, 22dp radius, soft lift, with the
 * create action raised out of the top edge. Destinations and their order are unchanged.
 */
@Composable
private fun ShellBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit, create: ShellCreate, onOpenMenu: () -> Unit) {
    val c = LocalHqColors.current
    val pill = RoundedCornerShape(HqRadius.navPill)
    Box(Modifier.fillMaxWidth().background(c.canvas).navigationBarsPadding().padding(start = 11.dp, end = 11.dp, top = 28.dp, bottom = 10.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(14.dp, pill, ambientColor = c.textPrimary.copy(alpha = .15f), spotColor = c.textPrimary.copy(alpha = .15f))
                .clip(pill)
                .background(c.surfaceBase)
                .border(1.dp, c.borderSubtle, pill)
                .selectableGroup()
                .defaultMinSize(minHeight = 70.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(AppTab.HOME, selectedTab == AppTab.HOME, Modifier.weight(1f)) { onTabSelected(AppTab.HOME) }
            BottomNavItem(AppTab.TASKS, selectedTab == AppTab.TASKS, Modifier.weight(1f)) { onTabSelected(AppTab.TASKS) }
            // The slot is always reserved so destinations never shift when an action appears or goes.
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                when (create) {
                    ShellCreate.None -> Unit
                    is ShellCreate.Direct -> CreateButton(create.label, create.onClick)
                    is ShellCreate.Menu -> CreateButton("Create", onOpenMenu)
                }
            }
            BottomNavItem(AppTab.DISCOVER, selectedTab == AppTab.DISCOVER, Modifier.weight(1f)) { onTabSelected(AppTab.DISCOVER) }
            BottomNavItem(AppTab.PROFILE, selectedTab == AppTab.PROFILE, Modifier.weight(1f)) { onTabSelected(AppTab.PROFILE) }
        }
    }
}

/** Raised 54dp rounded-square action with a white ring, per the Figma `.plus-button`. */
@Composable
private fun CreateButton(label: String, onClick: () -> Unit) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(17.dp)
    Box(
        Modifier
            .offset(y = (-24).dp)
            .shadow(10.dp, shape, ambientColor = c.brandTeal.copy(alpha = .35f), spotColor = c.brandTeal.copy(alpha = .35f))
            .size(54.dp)
            .clip(shape)
            .background(c.actionPrimaryBg)
            .border(4.dp, c.surfaceBase, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(HqIcons.Plus, null, tint = c.actionPrimaryFg, modifier = Modifier.size(27.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateMenuSheet(options: List<CreateOption>, onDismiss: () -> Unit) {
    val c = LocalHqColors.current
    // Figma "Quick add": a heading with a close button, then icon-tile rows with a title and a support line.
    HqBottomSheet(onDismiss = onDismiss) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text("Quick add", style = HqType.titleMedium2, color = c.textPrimary)
                Text("Create anything from wherever you are.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = 4.dp))
            }
            Box(
                Modifier.size(HqSize.target).clickable(role = Role.Button, onClick = onDismiss).semantics { contentDescription = "Close" },
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(34.dp).clip(CircleShape).background(c.surfaceSubtle), contentAlignment = Alignment.Center) {
                    Text("\u00D7", style = HqType.titleMedium2, color = c.textPrimary)
                }
            }
        }
        Spacer(Modifier.height(HqSpacing.md))
        options.forEachIndexed { index, option ->
            val look = quickAddLook(option.label)
            HqMenuRow(
                title = option.label,
                support = look.support,
                icon = look.icon,
                tone = look.tone,
                lastRow = index == options.lastIndex,
                onClick = { onDismiss(); option.onClick() },
            )
        }
        Spacer(Modifier.height(HqSpacing.xxl))
    }
}

private data class QuickAddLook(val icon: ImageVector, val tone: HqTileTone, val support: String?)

private fun quickAddLook(label: String): QuickAddLook = when {
    label.contains("task", ignoreCase = true) -> QuickAddLook(HqIcons.Check, HqTileTone.Teal, "Create a shared responsibility")
    label.contains("expense", ignoreCase = true) -> QuickAddLook(HqIcons.Receipt, HqTileTone.Sand, "Split a daily spend")
    label.contains("bill", ignoreCase = true) -> QuickAddLook(HqIcons.Clock, HqTileTone.Coral, "Set up a recurring household bill")
    label.contains("vacancy", ignoreCase = true) -> QuickAddLook(HqIcons.Discover, HqTileTone.Neutral, "List a room in your flat")
    label.contains("looking", ignoreCase = true) -> QuickAddLook(HqIcons.Discover, HqTileTone.Neutral, "Say what you are looking for")
    else -> QuickAddLook(HqIcons.Plus, HqTileTone.Teal, null)
}

@Composable
private fun BottomNavItem(tab: AppTab, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = LocalHqColors.current
    // Selected = brand-dark colour + heavier label + selected semantics (never colour alone).
    val color = if (selected) c.textBrand else c.textMuted
    Column(
        modifier
            .defaultMinSize(minWidth = HqSize.target, minHeight = HqSize.target)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(HqIconSize.md))
        Spacer(Modifier.height(HqSpacing.xs))
        Text(
            tab.label,
            style = HqType.labelSmall,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
