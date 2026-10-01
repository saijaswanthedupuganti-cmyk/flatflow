package habitiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import habitiq.app.ui.theme.HqRadius
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
    val shape = RoundedCornerShape(HqRadius.full)
    val background = if (selected) c.brandPrimaryContainer else c.surface
    val border = if (selected) c.brandPrimary else c.borderDefault
    val textColor = if (selected) c.brandPrimary else c.textSecondary

    var base = modifier
        .clip(shape)
        .background(background)
        .border(1.dp, border, shape)
    if (onClick != null) {
        base = base
            .defaultMinSize(minHeight = habitiq.app.ui.theme.HqTouchTarget)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.Checkbox,
                onClick = onClick
            )
    }

    Row(
        modifier = base.padding(horizontal = HqSpacing.md, vertical = HqSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected && onClick != null) {
            Icon(Icons.Default.Check, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
        }
        Text(label, style = HqType.labelMedium, color = if (enabled) textColor else c.textDisabled)
    }
}
