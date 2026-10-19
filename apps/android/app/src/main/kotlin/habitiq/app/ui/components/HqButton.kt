package habitiq.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqColorScheme
import habitiq.app.ui.theme.HqIconSize

import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqColorSpec
import habitiq.app.ui.theme.hqFocusRing
import habitiq.app.ui.theme.hqPressScale

import habitiq.app.ui.theme.rememberHqHaptics

/**
 * Button variants (design doc section 18). Exactly one [Primary] per screen (Von Restorff). A
 * destructive action never reuses the brand fill.
 */
enum class HqButtonVariant {
    /** Filled brand. The one thing this screen exists to do. */
    Primary,
    /** Tonal (soft brand fill). A real alternative next to a Primary: "Previous", "Add bill". */
    Secondary,
    /** Text only. Low-emphasis actions: "Skip", "Learn more", dialog dismiss. */
    Tertiary,
    /** Filled danger. Irreversible actions, usually inside a confirm dialog. */
    Destructive,
    /** White outline + white label for use on photos and dark hero art only. */
    GhostOnDark,
}

/**
 * The three button heights. Typography and corner radius are identical at every size; only height
 * and horizontal padding change. Every size keeps a 48dp touch target (Small is 40dp visual inside it).
 */
enum class HqButtonSize(val height: Dp, val paddingX: Dp) {
    /** 56dp. Full-width form submit / screen CTA pinned at the bottom. */
    Large(56.dp, HqSpacing.xxl),
    /** 48dp. Inline actions, dialogs, empty states, side-by-side pairs. */
    Medium(48.dp, HqSpacing.xl),
    /** 40dp visual, 48dp target. Dense rows and cards. */
    Small(40.dp, HqSpacing.lg),
}

private data class HqButtonColors(val container: Color, val content: Color, val border: Color?, val stateLayer: Color, val disabledContainer: Color, val disabledContent: Color, val disabledBorder: Color?)

private fun HqColorScheme.buttonColors(variant: HqButtonVariant): HqButtonColors {
    val off = textPrimary.copy(alpha = 0.38f)
    return when (variant) {
        HqButtonVariant.Primary -> HqButtonColors(actionPrimaryBg, actionPrimaryFg, null, stateLayerOnFill, disabledBg, off, null)
        HqButtonVariant.Secondary -> HqButtonColors(selectedBg, selectedFg, null, stateLayer, disabledBg, off, null)
        HqButtonVariant.Tertiary -> HqButtonColors(Color.Transparent, actionTertiaryFg, null, stateLayer, Color.Transparent, off, null)
        HqButtonVariant.Destructive -> HqButtonColors(actionDangerBg, actionDangerFg, null, stateLayerOnFill, disabledBg, off, null)
        HqButtonVariant.GhostOnDark -> HqButtonColors(Color.Transparent, onImage, onImage.copy(alpha = 0.72f), onImage, Color.Transparent, onImage.copy(alpha = 0.38f), onImage.copy(alpha = 0.24f))
    }
}

/**
 * The one button. Every variant, size and state lives here -- never a raw Material Button, never a
 * one-off `TaskButton` (design doc section 59).
 *
 * States: pressed = spring to 97% + a state-layer tint + a ripple clipped to the shape; focused =
 * 2dp focus ring outside the shape; disabled = neutral fill, 38% label, announced as disabled
 * (prefer leaving it enabled and validating on press); [loading] = a spinner replaces the label in
 * place (the label stays measured so the width never jumps), presses are ignored and TalkBack hears
 * "Loading".
 *
 * @param size defaults to [HqButtonSize.Large] when [fullWidth], [HqButtonSize.Medium] otherwise.
 * @param haptic plays the "confirm" haptic on press. Use on the primary confirm of a meaningful
 *   action (complete task, record payment), not on navigation.
 * @param interactionSource hoist to observe or drive press/focus (previews, the component gallery).
 */
@Composable
fun HqButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HqButtonVariant = HqButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    size: HqButtonSize? = null,
    haptic: Boolean = false,
    loadingDescription: String = "Loading",
    interactionSource: MutableInteractionSource? = null,
) {
    val c = LocalHqColors.current
    val colors = c.buttonColors(variant)
    val resolvedSize = size ?: if (fullWidth) HqButtonSize.Large else HqButtonSize.Medium
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    val haptics = rememberHqHaptics()
    val interactive = enabled && !loading
    val shape = HqShapes.button
    // Icons grow with the person's text size (up to 1.6x) so a 2x label never sits next to a tiny glyph.
    val iconSize = HqIconSize.sm * LocalDensity.current.fontScale.coerceIn(1f, 1.6f)

    val container by animateColorAsState(if (enabled) colors.container else colors.disabledContainer, hqColorSpec(), label = "btnBg")
    val content by animateColorAsState(if (enabled) colors.content else colors.disabledContent, hqColorSpec(), label = "btnFg")
    val layer by animateColorAsState(
        if (pressed && interactive) colors.stateLayer.copy(alpha = if (variant == HqButtonVariant.Primary || variant == HqButtonVariant.Destructive) 0.12f else 0.08f) else colors.stateLayer.copy(alpha = 0f),
        hqColorSpec(), label = "btnLayer",
    )
    val border = if (enabled) colors.border else colors.disabledBorder

    // Outer box: the full 48dp+ touch target and the semantics. Inner box: the visual button.
    Box(
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier)
            .defaultMinSize(minHeight = HqSize.target, minWidth = HqSize.target)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = interactive,
                role = Role.Button,
                onClick = {
                    if (haptic) haptics.confirm()
                    onClick()
                },
            )
            .then(if (loading) Modifier.semantics { stateDescription = loadingDescription; liveRegion = LiveRegionMode.Polite } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                .hqPressScale(pressed && interactive)
                .hqFocusRing(focused, c.focus, shape)
                .clip(shape)
                .background(container)
                .background(layer)
                .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
                .indication(interaction, ripple(color = content))
                .defaultMinSize(minHeight = resolvedSize.height)
                .padding(
                    start = if (leadingIcon != null) resolvedSize.paddingX - HqSpacing.xs else resolvedSize.paddingX,
                    end = if (trailingIcon != null) resolvedSize.paddingX - HqSpacing.xs else resolvedSize.paddingX,
                    // Room above and below the label so it never touches the edge when text is enlarged.
                    top = HqSpacing.md,
                    bottom = HqSpacing.md,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // The label row is always laid out so a loading button keeps its exact width.
            Row(
                Modifier.alpha(if (loading) 0f else 1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(iconSize))
                    Box(Modifier.width(HqSpacing.sm))
                }
                // Wraps only between words, in balanced lines; a word is never split ("Dele / te").
                Text(text, style = ButtonLabelStyle, color = content, textAlign = TextAlign.Center, modifier = Modifier.weight(1f, fill = false))
                if (trailingIcon != null) {
                    Box(Modifier.width(HqSpacing.sm))
                    Icon(trailingIcon, contentDescription = null, tint = content, modifier = Modifier.size(iconSize))
                }
            }
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(iconSize), strokeWidth = 2.5.dp, color = content)
            }
        }
    }
}

private val ButtonLabelStyle = HqType.buttonLabel.copy(lineBreak = LineBreak.Heading, hyphens = Hyphens.None)

/**
 * Two buttons that belong together ("Back" + "Continue", "Cancel" + "Delete"). Side by side, equal
 * width, when both labels fit on one line; otherwise stacked full width with [primary] at the bottom,
 * nearest the thumb. Never squeezes a label until its words break -- use this, not a Row, for any
 * pair of buttons.
 *
 * Pass buttons with the default `fullWidth = true`; this layout decides their width.
 */
@Composable
fun HqButtonPair(
    secondary: @Composable () -> Unit,
    primary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    gap: Dp = HqSpacing.md,
) {
    Layout(contents = listOf(secondary, primary), modifier = modifier.fillMaxWidth()) { (sec, pri), constraints ->
        val gapPx = gap.roundToPx()
        val width = constraints.maxWidth
        val half = (width - gapPx) / 2
        val s = sec.firstOrNull()
        val p = pri.firstOrNull()
        // Each label must fit in half the row on one line, plus the button's own padding.
        val fitsSideBySide = listOfNotNull(s, p).all { it.maxIntrinsicWidth(Constraints.Infinity) <= half }
        if (fitsSideBySide) {
            val cell = Constraints.fixedWidth(half)
            val sp = s?.measure(cell)
            val pp = p?.measure(cell)
            val height = maxOf(sp?.height ?: 0, pp?.height ?: 0)
            layout(width, height) {
                sp?.place(0, (height - sp.height) / 2)
                pp?.place(width - pp.width, (height - pp.height) / 2)
            }
        } else {
            val full = Constraints.fixedWidth(width)
            val sp = s?.measure(full)
            val pp = p?.measure(full)
            val height = (sp?.height ?: 0) + (if (sp != null && pp != null) gapPx else 0) + (pp?.height ?: 0)
            layout(width, height) {
                sp?.place(0, 0)
                pp?.place(0, height - pp.height)
            }
        }
    }
}

/**
 * Low-emphasis text-only action ("Learn more", "Skip", "Edit") -- design doc section 18. Same label
 * size as the brand [HqButtonVariant.Tertiary] but sized to its text, for use inline in rows and
 * headers. [color] defaults to the brand tint; pass `textSecondary` for a neutral low-emphasis action
 * or `statusDangerFg` for a destructive *text* action.
 */
@Composable
fun HqTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color? = null,
    leadingIcon: ImageVector? = null,
) {
    val c = LocalHqColors.current
    val resolved = if (enabled) (color ?: c.actionTertiaryFg) else c.textPrimary.copy(alpha = 0.38f)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    // The label is centred inside the full 48dp target, so it lines up with neighbouring text.
    Box(
        modifier
            .defaultMinSize(minHeight = HqSize.target, minWidth = HqSize.target)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .hqPressScale(pressed && enabled)
                .hqFocusRing(focused, c.focus, HqShapes.control)
                .clip(HqShapes.control)
                .indication(interaction, ripple(color = resolved))
                .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs + 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs + 2.dp),
        ) {
            if (leadingIcon != null) Icon(leadingIcon, null, tint = resolved, modifier = Modifier.size(HqIconSize.sm * LocalDensity.current.fontScale.coerceIn(1f, 1.6f)))
            Text(text, style = HqType.labelMedium.copy(fontWeight = HqType.buttonLabel.fontWeight), color = resolved)
        }
    }
}

/** Visual weight of an [HqIconButton]. */
enum class HqIconButtonStyle {
    /** Icon only (app bars, row trailing actions). */
    Standard,
    /** Icon on a soft circular fill (floating over content, secondary emphasis). */
    Tonal,
}

/**
 * Icon-only action: 24dp glyph, 40dp visual circle, 48dp target. [contentDescription] is required
 * and must name the action and its object ("More options for Kitchen cleanup"), never just "More".
 */
@Composable
fun HqIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: HqIconButtonStyle = HqIconButtonStyle.Standard,
    tint: Color? = null,
) {
    val c = LocalHqColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    val iconTint = if (!enabled) c.textPrimary.copy(alpha = 0.38f) else tint ?: if (style == HqIconButtonStyle.Tonal) c.selectedFg else c.iconDefault
    Box(
        modifier
            .size(HqSize.target)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(40.dp)
                .hqPressScale(pressed && enabled, pressedScale = 0.92f)
                .hqFocusRing(focused, c.focus, CircleShape)
                .clip(CircleShape)
                .then(if (style == HqIconButtonStyle.Tonal) Modifier.background(c.selectedBg) else Modifier)
                .indication(interaction, ripple(color = iconTint)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(HqIconSize.md))
        }
    }
}
