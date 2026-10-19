package habitiq.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqMotion
import habitiq.app.ui.theme.HqShadow
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqColorSpec
import habitiq.app.ui.theme.hqFocusRing
import habitiq.app.ui.theme.hqReduceMotion
import habitiq.app.ui.theme.hqShadow
import habitiq.app.ui.theme.rememberHqHaptics
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/* =================================================================================================
 * Segmented control
 * ===============================================================================================*/

private val SegmentInset = 4.dp

/**
 * A few mutually exclusive views of the same content on one track (Tasks / Expenses, My / All).
 *
 * - One raised pill slides to the selected segment on the layout spring (snaps under reduced motion).
 * - Every segment is a full 48dp-tall target; the track is 48dp at default text size and grows
 *   with the font -- labels wrap, they are never clipped or ellipsised.
 * - [equalWidth] (default) splits the width equally, for 2-4 short peers. `false` sizes segments to
 *   their labels and scrolls horizontally if they do not fit.
 * - Announced as tabs with selected state; a light haptic tick on change ([haptic]).
 */
@Composable
fun HqSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    equalWidth: Boolean = true,
    haptic: Boolean = true,
) {
    val c = LocalHqColors.current
    val haptics = rememberHqHaptics()
    val reduce = hqReduceMotion()
    val density = LocalDensity.current
    val track = HqShapes.button // 16 = 12 (indicator) + 4 (inset): concentric
    val pill = HqShapes.control

    // Measured x/width of each segment, in px, relative to the row.
    val cells = remember(options.size) { mutableStateMapOf<Int, Pair<Int, Int>>() }
    val target = cells[selectedIndex]
    val x = remember { Animatable(0f) }
    val w = remember { Animatable(0f) }
    var placed by remember { mutableStateOf(false) }
    LaunchedEffect(target, reduce) {
        val t = target ?: return@LaunchedEffect
        if (!placed || reduce) {
            x.snapTo(t.first.toFloat()); w.snapTo(t.second.toFloat()); placed = true
        } else {
            launch { x.animateTo(t.first.toFloat(), HqMotion.layoutSpring()) }
            launch { w.animateTo(t.second.toFloat(), HqMotion.layoutSpring()) }
        }
    }

    Box(modifier.fillMaxWidth().clip(track).background(c.segmentTrack)) {
        Box(
            Modifier
                .then(if (equalWidth) Modifier.fillMaxWidth() else Modifier.horizontalScroll(rememberScrollState()))
                .height(IntrinsicSize.Min),
        ) {
            // Indicator under the labels.
            if (placed) {
                Box(Modifier.matchParentSize()) {
                    Box(
                        Modifier
                            .offset { IntOffset(x.value.roundToInt(), 0) }
                            .width(with(density) { w.value.toDp() })
                            .fillMaxHeight()
                            .padding(SegmentInset)
                            .hqShadow(HqShadow.Floating, pill)
                            .clip(pill)
                            .background(c.segmentSelectedBg),
                    )
                }
            }
            Row(
                Modifier.then(if (equalWidth) Modifier.fillMaxWidth() else Modifier).height(IntrinsicSize.Min).selectableGroup(),
            ) {
                options.forEachIndexed { index, label ->
                    val selected = index == selectedIndex
                    val color by animateColorAsState(if (selected) c.textPrimary else c.textSecondary, hqColorSpec(), label = "segText")
                    val interaction = remember { MutableInteractionSource() }
                    val focused by interaction.collectIsFocusedAsState()
                    Box(
                        Modifier
                            .then(if (equalWidth) Modifier.weight(1f) else Modifier)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = HqSize.target, minWidth = HqSize.target)
                            .onPlaced { cells[index] = it.positionInParent().x.roundToInt() to it.size.width }
                            .selectable(
                                selected = selected,
                                interactionSource = interaction,
                                indication = null,
                                role = Role.Tab,
                                onClick = {
                                    if (index != selectedIndex) {
                                        if (haptic) haptics.tick()
                                        onSelect(index)
                                    }
                                },
                            )
                            .padding(SegmentInset)
                            .hqFocusRing(focused, c.focus, pill)
                            .clip(pill)
                            .indication(interaction, ripple(color = c.stateLayer)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            style = HqType.labelMedium,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = color,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = HqSpacing.md, vertical = HqSpacing.xs + 2.dp),
                        )
                    }
                }
            }
        }
    }
}

/* =================================================================================================
 * Switch
 * ===============================================================================================*/

private val TrackWidth = 52.dp
private val TrackHeight = 32.dp
private val ThumbSize = 24.dp
private val ThumbSizeOff = 16.dp
private val ThumbInset = 4.dp

/**
 * The visual switch, shared by [HqSwitch] and [HqSwitchRow]. The thumb travels on the press spring,
 * stretches (squash) while held and mid-travel, and the track colour follows the thumb.
 */
@Composable
private fun HqSwitchVisual(checked: Boolean, enabled: Boolean, pressed: Boolean, focused: Boolean, thumbIcon: ImageVector?) {
    val c = LocalHqColors.current
    val reduce = hqReduceMotion()
    val progress by animateFloatAsState(
        if (checked) 1f else 0f,
        if (reduce) androidx.compose.animation.core.snap() else HqMotion.pressSpring(),
        label = "switchProgress",
    )
    val p = progress.coerceIn(0f, 1f)
    // Android's switch language: off = light track with an outline and a small grey knob; on = solid
    // teal with a full-size white knob. The knob grows while held, and stretches a little mid-travel.
    val pressGrow by animateFloatAsState(if (pressed && !reduce) 1f else 0f, HqMotion.pressSpring(), label = "switchPress")
    val travelStretch = if (reduce) 0f else 1f - abs(2f * p - 1f)
    val knob = lerpDp(ThumbSizeOff, ThumbSize, p) + 4.dp * pressGrow
    val thumbW = knob + 4.dp * travelStretch
    val travel = TrackWidth - ThumbInset * 2 - ThumbSize
    val centerOffset = (ThumbSize - thumbW) / 2
    val track = lerp(c.surfaceSubtle, c.controlOn, p)
    val outline = c.controlOff.copy(alpha = 1f - p)
    val thumb = lerp(c.controlOff, c.onControlOn, p)

    Box(
        Modifier
            .alpha(if (enabled) 1f else 0.38f)
            .hqFocusRing(focused, c.focus, CircleShape)
            .size(TrackWidth, TrackHeight)
            .clip(CircleShape)
            .background(track)
            .border(2.dp, outline, CircleShape),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = ThumbInset + travel * p + centerOffset)
                .size(thumbW, knob)
                .clip(CircleShape)
                .background(thumb),
            contentAlignment = Alignment.Center,
        ) {
            if (thumbIcon != null) {
                Icon(thumbIcon, null, tint = c.controlOn, modifier = Modifier.size(HqIconSize.xs).graphicsLayer { alpha = p })
            }
        }
    }
}

/**
 * On/off for a setting that applies immediately (no Save button). 52x32 track, 24dp thumb, 48dp
 * touch target, announced as a Switch with "On"/"Off". Prefer [HqSwitchRow], which makes the whole
 * labelled row the target; use the bare switch only where a row already has a label.
 *
 * @param thumbIcon optional glyph inside the thumb when on (e.g. Icons.Filled.Check).
 * @param haptic toggle haptic on change.
 */
@Composable
fun HqSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    thumbIcon: ImageVector? = null,
    haptic: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    val haptics = rememberHqHaptics()
    Box(
        modifier
            .defaultMinSize(minWidth = HqSize.target, minHeight = HqSize.target)
            .toggleable(
                value = checked,
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = {
                    if (haptic) haptics.toggle(it)
                    onCheckedChange(it)
                },
            )
            .semantics {
                stateDescription = if (checked) "On" else "Off"
                if (contentDescription != null) this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        HqSwitchVisual(checked, enabled, pressed, focused, thumbIcon)
    }
}

/**
 * A labelled setting with a switch. The whole row (min 56dp, 72dp with [supporting]) is one Switch
 * target, so the label is part of what is announced and tapped.
 */
@Composable
fun HqSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    thumbIcon: ImageVector? = null,
    haptic: Boolean = true,
) {
    val c = LocalHqColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    val haptics = rememberHqHaptics()
    HqSelectionRowLayout(
        title = title,
        supporting = supporting,
        enabled = enabled,
        leadingIcon = leadingIcon,
        modifier = modifier
            .toggleable(
                value = checked,
                interactionSource = interaction,
                indication = ripple(color = c.stateLayer),
                enabled = enabled,
                role = Role.Switch,
                onValueChange = {
                    if (haptic) haptics.toggle(it)
                    onCheckedChange(it)
                },
            )
            .semantics { stateDescription = if (checked) "On" else "Off" },
        leading = null,
        trailing = { HqSwitchVisual(checked, enabled, pressed, focused, thumbIcon) },
    )
}

/* =================================================================================================
 * Checkbox and radio
 * ===============================================================================================*/

private val BoxSize = 20.dp

/** Checkbox glyph (visual only). Always used inside a row or other labelled toggleable. */
@Composable
fun HqCheckbox(checked: Boolean, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = LocalHqColors.current
    val reduce = hqReduceMotion()
    val fill by animateColorAsState(if (checked) c.controlOn else c.controlOn.copy(alpha = 0f), hqColorSpec(), label = "cbFill")
    val border by animateColorAsState(if (checked) c.controlOn else c.controlOff, hqColorSpec(), label = "cbBorder")
    val tick by animateFloatAsState(if (checked) 1f else 0f, if (reduce) androidx.compose.animation.core.snap() else HqMotion.pressSpring(), label = "cbTick")
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier.alpha(if (enabled) 1f else 0.38f).size(BoxSize).clip(shape).background(fill).border(2.dp, border, shape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Check, null, tint = c.onControlOn,
            modifier = Modifier.size(16.dp).graphicsLayer { val s = 0.6f + 0.4f * tick; scaleX = s; scaleY = s; alpha = tick.coerceIn(0f, 1f) },
        )
    }
}

/** Radio glyph (visual only). Always used inside [HqRadioRow] or another labelled selectable. */
@Composable
fun HqRadio(selected: Boolean, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = LocalHqColors.current
    val reduce = hqReduceMotion()
    val border by animateColorAsState(if (selected) c.controlOn else c.controlOff, hqColorSpec(), label = "rbBorder")
    val dot by animateDpAsState(if (selected) 10.dp else 0.dp, if (reduce) androidx.compose.animation.core.snap() else HqMotion.pressSpring(), label = "rbDot")
    Box(
        modifier.alpha(if (enabled) 1f else 0.38f).size(BoxSize).clip(CircleShape).border(2.dp, border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(dot).clip(CircleShape).background(c.controlOn))
    }
}

/** A labelled checkbox row: the whole 56/72dp row toggles, announced as a Checkbox. */
@Composable
fun HqCheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    enabled: Boolean = true,
) {
    val c = LocalHqColors.current
    HqSelectionRowLayout(
        title = title,
        supporting = supporting,
        enabled = enabled,
        leadingIcon = null,
        modifier = modifier.toggleable(
            value = checked,
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = c.stateLayer),
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        ),
        leading = { HqCheckbox(checked, enabled = enabled) },
        trailing = null,
    )
}

/**
 * A labelled radio row: the whole row selects, announced as a RadioButton. Put sibling rows in a
 * container with `Modifier.selectableGroup()`, or use [HqRadioGroup].
 */
@Composable
fun HqRadioRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    enabled: Boolean = true,
) {
    val c = LocalHqColors.current
    HqSelectionRowLayout(
        title = title,
        supporting = supporting,
        enabled = enabled,
        leadingIcon = null,
        modifier = modifier.selectable(
            selected = selected,
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = c.stateLayer),
            enabled = enabled,
            role = Role.RadioButton,
            onClick = onClick,
        ),
        leading = { HqRadio(selected, enabled = enabled) },
        trailing = null,
    )
}

/** One-of-many choice as a vertical list of [HqRadioRow]s, grouped for TalkBack. */
@Composable
fun HqRadioGroup(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, supporting: List<String?>? = null) {
    Column(modifier.fillMaxWidth().selectableGroup()) {
        options.forEachIndexed { i, label ->
            HqRadioRow(title = label, selected = i == selectedIndex, onClick = { onSelect(i) }, supporting = supporting?.getOrNull(i))
        }
    }
}

/** Shared row geometry for switch / checkbox / radio rows: 16dp sides, 12dp vertical, 56/72dp min. */
@Composable
private fun HqSelectionRowLayout(
    title: String,
    supporting: String?,
    enabled: Boolean,
    leadingIcon: ImageVector?,
    modifier: Modifier,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
) {
    val c = LocalHqColors.current
    Row(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = if (supporting != null) HqSize.rowTwoLine else HqSize.row)
            .padding(horizontal = HqSpacing.component, vertical = HqSpacing.related),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.related + 4.dp),
    ) {
        if (leading != null) leading()
        if (leadingIcon != null) Icon(leadingIcon, null, tint = if (enabled) c.iconDefault else c.disabledFg, modifier = Modifier.size(HqIconSize.md))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = HqType.rowTitle, color = if (enabled) c.textPrimary else c.disabledFg)
            if (supporting != null) Text(supporting, style = HqType.bodyMedium, color = if (enabled) c.textSecondary else c.disabledFg)
        }
        if (trailing != null) trailing()
    }
}

