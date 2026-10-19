package habitiq.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqMotion
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqColorSpec
import habitiq.app.ui.theme.hqFocusRing
import habitiq.app.ui.theme.hqPressScale
import habitiq.app.ui.theme.hqReduceMotion

/**
 * What a chip means, which decides its role and its selected mark.
 * - [Filter]: one of several independent on/off filters (multi-select). Role Checkbox; check mark when on.
 * - [Choice]: one-of-many in a small set, shown inline (single-select). Role RadioButton; no check,
 *   the filled state carries it. Put the chips in a `Modifier.selectableGroup()` container.
 * Input chips (a removable value) are [HqRemovableChip].
 */
enum class HqChipKind { Filter, Choice }

/**
 * Compact filter / choice / status chip (design doc section 49): 32dp visual pill inside a 48dp
 * target. Not body text -- only filters, selected criteria, statuses and short metadata ("Weekly").
 * Without [onClick] it is a read-only label with no role and no target.
 */
@Composable
fun HqChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    kind: HqChipKind = HqChipKind.Filter,
    leadingIcon: ImageVector? = null,
) {
    val c = LocalHqColors.current
    val reduce = hqReduceMotion()
    val shape = HqShapes.pill
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()

    // Filled, borderless pills (current Android/iOS style): unselected = soft grey with dark text,
    // selected = solid teal with white text and a check. Read-only chips stay quiet grey.
    val background by animateColorAsState(
        when {
            !enabled -> c.surfaceSubtle.copy(alpha = 0.6f)
            selected -> c.actionPrimaryBg
            else -> c.surfaceSubtle
        },
        hqColorSpec(), label = "chipBg",
    )
    val textColor by animateColorAsState(
        when {
            !enabled -> c.textPrimary.copy(alpha = 0.38f)
            selected -> c.actionPrimaryFg
            onClick == null -> c.textSecondary
            else -> c.textPrimary
        },
        hqColorSpec(), label = "chipFg",
    )
    val iconSize = HqIconSize.xs * LocalDensity.current.fontScale.coerceIn(1f, 1.6f)

    val target = if (onClick != null) {
        modifier
            .defaultMinSize(minHeight = HqSize.target, minWidth = HqSize.target)
            .selectable(
                selected = selected,
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                role = if (kind == HqChipKind.Choice) Role.RadioButton else Role.Checkbox,
                onClick = onClick,
            )
    } else {
        modifier
    }

    Box(modifier = target, contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .hqPressScale(pressed && enabled && onClick != null)
                .hqFocusRing(focused, c.focus, shape)
                .defaultMinSize(minHeight = HqSize.chipVisual)
                .clip(shape)
                .background(background)
                .indication(interaction, ripple(color = textColor))
                .padding(horizontal = HqSpacing.md + 2.dp, vertical = HqSpacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = textColor, modifier = Modifier.padding(end = HqSpacing.xs).size(iconSize))
            }
            // The check grows in from the label's leading edge (filter chips only), so selection reads as an addition.
            AnimatedVisibility(
                visible = selected && kind == HqChipKind.Filter && leadingIcon == null,
                enter = if (reduce) fadeIn(HqMotion.fastTween()) else expandHorizontally(HqMotion.layoutSpring()) + fadeIn(HqMotion.fastTween()),
                exit = if (reduce) fadeOut(HqMotion.fastTween()) else shrinkHorizontally(HqMotion.layoutSpring()) + fadeOut(HqMotion.fastTween()),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = textColor, modifier = Modifier.padding(end = HqSpacing.xs).size(iconSize))
            }
            Text(label, style = HqType.labelMedium.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium), color = textColor)
        }
    }
}
