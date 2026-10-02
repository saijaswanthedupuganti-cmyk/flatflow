package habitiq.app.ui.components

import habitiq.app.ui.theme.hqPressable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Figma `.profile-group`: a white 20dp card with a tracked-caps label and divided menu rows.
 * Rows are [HqMenuRow]; the last row loses its divider automatically via [lastRow].
 */
@Composable
fun HqMenuGroup(label: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.fillMaxWidth()
            .shadow(3.dp, shape, ambientColor = c.textPrimary.copy(alpha = .05f), spotColor = c.textPrimary.copy(alpha = .05f))
            .clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
            .padding(horizontal = 14.dp, vertical = 4.dp),
    ) {
        if (label != null) {
            Text(label.uppercase(), style = HqType.labelSmall, color = c.textBrand, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 2.dp, top = 11.dp, bottom = 4.dp))
        }
        content()
    }
}

/** Figma `.menu-row`: optional icon tile, title and support, trailing chevron, hairline below unless [lastRow]. */
@Composable
fun HqMenuRow(
    title: String,
    modifier: Modifier = Modifier,
    support: String? = null,
    icon: ImageVector? = null,
    tone: HqTileTone = HqTileTone.Teal,
    danger: Boolean = false,
    lastRow: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit,
) {
    val c = LocalHqColors.current
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp).hqPressable(onClick = onClick).padding(vertical = 13.dp, horizontal = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) HqIconTile(icon, tone, size = 38)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = HqType.rowTitle, color = if (danger) c.statusDangerFg else c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (support != null) Text(support, style = HqType.bodyMedium, color = c.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (showChevron) Icon(HqIcons.Chevron, null, tint = c.iconDefault, modifier = Modifier.size(HqIconSize.sm))
        }
        if (!lastRow) Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
    }
}

/** Figma `.sign-out`: full-width coral-soft button with danger text, for destructive account actions. */
@Composable
fun HqDangerSoftButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    Box(
        modifier.fillMaxWidth().defaultMinSize(minHeight = 49.dp).clip(RoundedCornerShape(15.dp)).background(c.warmBg)
            .hqPressable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = HqType.buttonLabel, color = c.statusDangerFg, fontWeight = FontWeight.Bold) }
}

/** Figma `.setting-row`: title and support on the left, a control (usually [HqSwitch]) on the right. */
@Composable
fun HqSettingRow(title: String, support: String?, modifier: Modifier = Modifier, control: @Composable () -> Unit) {
    val c = LocalHqColors.current
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 15.dp, horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = HqType.rowTitle, color = c.textPrimary)
                if (support != null) Text(support, style = HqType.bodyMedium, color = c.textSecondary)
            }
            control()
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
    }
}
