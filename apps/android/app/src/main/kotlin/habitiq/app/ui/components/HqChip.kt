package habitiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Compact metadata / filter / status chip (design doc section 49). Not a replacement for body
 * text -- use only for filters, selected criteria, statuses, or short metadata like "Weekly" or
 * "Hyderabad".
 */
@Composable
fun HqChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(HqRadius.pill)
    // chip.* tokens: unselected keeps the visible control boundary; selected adds fill + 2dp border + check.
    val background = if (selected) c.selectedBg else c.surfaceBase
    val borderColor = when {
        !enabled -> c.disabledBorder
        selected -> c.selectedBorder
        else -> c.borderControl
    }
    val borderWidth = if (selected) 2.dp else 1.dp
    val textColor = when {
        !enabled -> c.disabledFg
        selected -> c.selectedFg
        else -> c.textSecondary
    }

    // The 48dp interaction area wraps a 32dp visual chip, so a small chip never becomes a small target.
    val target = if (onClick != null) {
        modifier
            .defaultMinSize(minHeight = HqSize.target)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.Checkbox,
                onClick = onClick,
            )
    } else {
        modifier
    }

    Box(modifier = target, contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = HqSize.chipVisual)
                .clip(shape)
                .background(background)
                .border(borderWidth, borderColor, shape)
                .padding(horizontal = HqSpacing.md, vertical = HqSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = textColor, modifier = Modifier.size(HqIconSize.xs))
            }
            Text(label, style = HqType.labelMedium, color = textColor)
        }
    }
}
