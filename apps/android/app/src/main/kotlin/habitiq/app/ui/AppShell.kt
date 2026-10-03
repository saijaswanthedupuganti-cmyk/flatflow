package habitiq.app.ui

import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqMenuRow
import androidx.compose.foundation.background
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.rounded.Mic
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
    onMic: (() -> Unit)? = null,
    content: @Composable (AppTab) -> Unit
) {
    val c = LocalHqColors.current
    var menuOpen by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(c.canvas)) {
        val reduceMotion = habitiq.app.ui.theme.hqReduceMotion()
        // Material fade-through between top-level destinations: quick fade out, then fade + slight scale in.
        Box(Modifier.weight(1f)) {
        androidx.compose.animation.AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (reduceMotion) {
                    androidx.compose.animation.EnterTransition.None togetherWith androidx.compose.animation.ExitTransition.None
                } else {
                    (androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(210, delayMillis = 70)) +
                        androidx.compose.animation.scaleIn(androidx.compose.animation.core.tween(210, delayMillis = 70), initialScale = 0.985f)) togetherWith
                        androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(90))
                }
            },
            label = "tabs",
        ) { tab -> Box(Modifier.fillMaxSize()) { content(tab) } }
            if (createAction is ShellCreate.Menu) {
                QuickAddOverlay(open = menuOpen, options = createAction.options) { menuOpen = false }
            }
        }
        val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        if (!keyboardOpen) {
            ShellBar(
                selectedTab = selectedTab,
                onTabSelected = { menuOpen = false; onTabSelected(it) },
                create = createAction,
                menuOpen = menuOpen,
                onMic = onMic?.let { mic -> { menuOpen = false; mic() } },
            ) { menuOpen = !menuOpen }
        }
    }
    androidx.activity.compose.BackHandler(enabled = menuOpen) { menuOpen = false }
}

/**
 * Floating pill from the Figma Make file: inset from the screen edges, 22dp radius, soft lift, with the
 * create action raised out of the top edge. Destinations and their order are unchanged.
 */
@Composable
private fun ShellBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit, create: ShellCreate, menuOpen: Boolean, onMic: (() -> Unit)?, onOpenMenu: () -> Unit) {
    val c = LocalHqColors.current
    val pill = RoundedCornerShape(HqRadius.navPill)
    Box(Modifier.fillMaxWidth().background(c.canvas)) {
      Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 11.dp, end = 11.dp, top = 28.dp, bottom = 10.dp)) {
        // The pill surface is drawn as a separate layer so the raised create button is never clipped by it.
        Box(
            Modifier
                .matchParentSize()
                .shadow(14.dp, pill, ambientColor = c.textPrimary.copy(alpha = .15f), spotColor = c.textPrimary.copy(alpha = .15f))
                .clip(pill)
                .background(c.surfaceBase)
                .border(1.dp, c.borderSubtle, pill)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .selectableGroup()
                .defaultMinSize(minHeight = 70.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(AppTab.HOME, selectedTab == AppTab.HOME, Modifier.weight(1f)) { onTabSelected(AppTab.HOME) }
            BottomNavItem(AppTab.TASKS, selectedTab == AppTab.TASKS, Modifier.weight(1f)) { onTabSelected(AppTab.TASKS) }
            // The slot is always reserved so destinations never shift when an action appears or goes.
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                when {
                    onMic != null -> MicButton(
                        menuOpen = menuOpen,
                        onTap = { if (menuOpen) onOpenMenu() else onMic() },
                        onLongPress = when (create) {
                            ShellCreate.None -> null
                            is ShellCreate.Direct -> create.onClick
                            is ShellCreate.Menu -> onOpenMenu
                        },
                    )
                    create is ShellCreate.Direct -> CreateButton(create.label, false, create.onClick)
                    create is ShellCreate.Menu -> CreateButton(if (menuOpen) "Close quick add" else "Quick add", menuOpen, onOpenMenu)
                    else -> Unit
                }
            }
            BottomNavItem(AppTab.DISCOVER, selectedTab == AppTab.DISCOVER, Modifier.weight(1f)) { onTabSelected(AppTab.DISCOVER) }
            BottomNavItem(AppTab.PROFILE, selectedTab == AppTab.PROFILE, Modifier.weight(1f)) { onTabSelected(AppTab.PROFILE) }
        }
      }
    }
}

/** Raised 54dp rounded-square action with a white ring (Figma `.plus-button`); springs into a x when open. */
@Composable
private fun CreateButton(label: String, open: Boolean, onClick: () -> Unit) {
    val c = LocalHqColors.current
    val reduceMotion = habitiq.app.ui.theme.hqReduceMotion()
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val rotation by animateFloatAsState(
        if (open) 135f else 0f,
        if (reduceMotion) snap() else spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "plusRotation",
    )
    val press by animateFloatAsState(if (pressed) 0.88f else 1f, spring(dampingRatio = 0.5f, stiffness = 900f), label = "plusPress")
    val fill by animateColorAsState(if (open) c.textPrimary else c.actionPrimaryBg, tween(220), label = "plusFill")
    val glyph by animateColorAsState(if (open) c.canvas else c.actionPrimaryFg, tween(220), label = "plusGlyph")
    val shape = RoundedCornerShape(17.dp)
    Box(
        Modifier
            .offset(y = (-24).dp)
            .graphicsLayer { scaleX = press; scaleY = press }
            .shadow(10.dp, shape, ambientColor = c.brandTeal.copy(alpha = .35f), spotColor = c.brandTeal.copy(alpha = .35f))
            .size(54.dp)
            .clip(shape)
            .background(fill)
            .border(4.dp, c.surfaceBase, shape)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(HqIcons.Plus, null, tint = glyph, modifier = Modifier.size(27.dp).graphicsLayer { rotationZ = rotation })
    }
}

/**
 * The agent entry: the same raised button as Quick add with a mic glyph. Tap talks to Oddroof;
 * long-press opens Quick add. While Quick add is open the button shows × and a tap closes it.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun MicButton(menuOpen: Boolean, onTap: () -> Unit, onLongPress: (() -> Unit)?) {
    val c = LocalHqColors.current
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 0.88f else 1f, spring(dampingRatio = 0.5f, stiffness = 900f), label = "micPress")
    val fill by animateColorAsState(if (menuOpen) c.textPrimary else c.actionPrimaryBg, tween(220), label = "micFill")
    val glyph by animateColorAsState(if (menuOpen) c.canvas else c.actionPrimaryFg, tween(220), label = "micGlyph")
    val shape = RoundedCornerShape(17.dp)
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    Box(
        Modifier
            .offset(y = (-24).dp)
            .graphicsLayer { scaleX = press; scaleY = press }
            .shadow(10.dp, shape, ambientColor = c.brandTeal.copy(alpha = .35f), spotColor = c.brandTeal.copy(alpha = .35f))
            .size(54.dp)
            .clip(shape)
            .background(fill)
            .border(4.dp, c.surfaceBase, shape)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClickLabel = if (menuOpen) "Close quick add" else "Talk to Oddroof",
                onLongClickLabel = if (onLongPress != null) "Quick add" else null,
                onLongClick = onLongPress?.let { action ->
                    { haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); action() }
                },
                onClick = onTap,
            )
            .semantics { contentDescription = if (menuOpen) "Close quick add" else "Talk to Oddroof" },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (menuOpen) HqIcons.Plus else androidx.compose.material.icons.Icons.Rounded.Mic,
            contentDescription = null,
            tint = glyph,
            modifier = Modifier.size(27.dp).graphicsLayer { rotationZ = if (menuOpen) 45f else 0f },
        )
    }
}

/**
 * Speed-dial Quick add. The content dims (the nav bar stays crisp), the + turns into a ×, and the
 * actions rise from the button as floating cards, nearest first, with a soft overshoot. Tap ×, the
 * dimmed area or Back to close; it reverses quickly. With system animations off it simply appears.
 */
@Composable
private fun QuickAddOverlay(open: Boolean, options: List<CreateOption>, onClose: () -> Unit) {
    val reduceMotion = habitiq.app.ui.theme.hqReduceMotion()
    val transition = updateTransition(targetState = open, label = "quickAdd")
    val scrim by transition.animateFloat(
        transitionSpec = { if (reduceMotion) snap() else tween(if (targetState) 220 else 160) },
        label = "scrim",
    ) { if (it) 1f else 0f }
    if (!open && !transition.isRunning && scrim == 0f) return

    val backOut = CubicBezierEasing(0.34f, 1.45f, 0.64f, 1f)
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxSize()
                .graphicsLayer { alpha = scrim }
                .background(Color(0xFF091C1A).copy(alpha = .58f))
                .pointerInput(Unit) { detectTapGestures { onClose() } },
        )
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "QUICK ADD",
                style = HqType.labelSmall,
                color = Color.White.copy(alpha = .85f),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp).graphicsLayer { alpha = scrim },
            )
            options.forEachIndexed { index, option ->
                val fromButton = options.lastIndex - index
                val p by transition.animateFloat(
                    transitionSpec = {
                        when {
                            reduceMotion -> snap()
                            targetState -> tween(340, delayMillis = 45 * fromButton, easing = backOut)
                            else -> tween(150, delayMillis = 25 * index)
                        }
                    },
                    label = "card$index",
                ) { if (it) 1f else 0f }
                QuickAddCard(
                    option = option,
                    modifier = Modifier.graphicsLayer {
                        alpha = p.coerceIn(0f, 1f)
                        translationY = (1f - p) * 36.dp.toPx()
                        val s = 0.9f + 0.1f * p
                        scaleX = s
                        scaleY = s
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
                    onClick = { onClose(); option.onClick() },
                )
            }
        }
    }
}

@Composable
private fun QuickAddCard(option: CreateOption, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = LocalHqColors.current
    val look = quickAddLook(option.label)
    val shape = RoundedCornerShape(18.dp)
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(if (pressed) 0.97f else 1f, tween(120), label = "cardPress")
    Row(
        modifier
            .graphicsLayer { scaleX *= press; scaleY *= press }
            .fillMaxWidth()
            .shadow(12.dp, shape, ambientColor = Color.Black.copy(alpha = .18f), spotColor = Color.Black.copy(alpha = .18f))
            .clip(shape)
            .background(c.surfaceRaised)
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        habitiq.app.ui.components.HqIconTile(look.icon, look.tone, size = 42)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(option.label, style = HqType.rowTitle, color = c.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
            look.support?.let { Text(it, style = HqType.bodySmall, color = c.textSecondary, maxLines = 1) }
        }
        Icon(HqIcons.Chevron, null, tint = c.textMuted, modifier = Modifier.size(HqIconSize.sm))
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
    val color by animateColorAsState(if (selected) c.textBrand else c.textMuted, tween(200), label = "navColor")
    val reduceMotion = habitiq.app.ui.theme.hqReduceMotion()
    val pill by animateFloatAsState(
        if (selected) 1f else 0f,
        if (reduceMotion) snap() else spring(dampingRatio = 0.62f, stiffness = 520f),
        label = "navPill",
    )
    val bounce = remember { androidx.compose.animation.core.Animatable(1f) }
    androidx.compose.runtime.LaunchedEffect(selected) {
        if (selected && !reduceMotion) {
            bounce.snapTo(0.82f)
            bounce.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 520f))
        }
    }
    Column(
        modifier
            .defaultMinSize(minWidth = HqSize.target, minHeight = HqSize.target)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Material 3 active indicator: grows out from the icon centre.
            Box(
                Modifier.size(width = 52.dp, height = 30.dp)
                    .graphicsLayer { scaleX = 0.4f + 0.6f * pill; alpha = pill.coerceIn(0f, 1f) }
                    .clip(RoundedCornerShape(HqRadius.pill))
                    .background(c.selectedBg),
            )
            Icon(
                tab.icon, contentDescription = null, tint = color,
                modifier = Modifier.size(HqIconSize.md).graphicsLayer { scaleX = bounce.value; scaleY = bounce.value },
            )
        }
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
