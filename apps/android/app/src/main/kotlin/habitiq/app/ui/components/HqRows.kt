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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * One neutral surface around a set of related rows with quiet internal dividers (design doc 10.5).
 * Rows inside never get their own border, shadow or tile.
 */
@Composable
fun HqGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(HqRadius.card)
    Column(
        modifier.fillMaxWidth().clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape),
        content = content,
    )
}

/** Hairline between two rows of a [HqGroup], inset so it lines up with the text column. */
@Composable
fun HqRowDivider(startInset: Dp = HqSpacing.component) {
    Box(Modifier.fillMaxWidth().padding(start = startInset).height(1.dp).background(LocalHqColors.current.borderSubtle))
}

/** Section title with an optional trailing action, centred on one line. A long title wraps inside its own column. */
@Composable
fun HqSectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    val c = LocalHqColors.current
    Row(
        modifier.fillMaxWidth().defaultMinSize(minHeight = HqSize.target),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
    ) {
        Text(title, style = HqType.titleSmall2, color = c.textPrimary, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) HqTextButton(text = action, onClick = onAction)
    }
}

/** Small non-interactive state label that sits beside the thing it describes. */
@Composable
fun HqStatusBadge(label: String, background: Color, foreground: Color, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier.clip(RoundedCornerShape(HqRadius.small)).background(background)
            .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs),
    ) {
        if (icon != null) Icon(icon, null, tint = foreground, modifier = Modifier.size(HqIconSize.xs))
        Text(label, style = HqType.labelSmall, color = foreground)
    }
}

/** A value with a plain label. No chevron, no button styling: it is information, not an action. */
@Composable
fun HqFact(value: String, label: String, modifier: Modifier = Modifier, valueColor: Color = LocalHqColors.current.textPrimary) {
    val c = LocalHqColors.current
    Column(modifier) {
        Text(value, style = HqType.titleSmall2, color = valueColor)
        Text(label, style = HqType.labelSmall, color = c.textSecondary)
    }
}

/**
 * Compact task row: name, then who and when, with the authorised completion control in its own
 * 48dp target so completing never also opens the detail (design doc 10.6).
 *
 * [dueText] carries urgency in words; [overdue] adds a clock cue rather than a red wash.
 */
@Composable
fun HqTaskRow(
    name: String,
    assigneeText: String,
    dueText: String,
    overdue: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    quiet: Boolean = false,
    canComplete: Boolean = false,
    completing: Boolean = false,
    onOpen: () -> Unit = {},
    onComplete: () -> Unit = {},
) {
    val c = LocalHqColors.current
    val titleColor = if (quiet) c.textSecondary else c.textPrimary
    Row(modifier.fillMaxWidth().defaultMinSize(minHeight = HqSize.rowTwoLine), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).clickable(role = Role.Button, onClick = onOpen)
                .padding(start = HqSpacing.component, top = HqSpacing.related, bottom = HqSpacing.related),
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.related),
            verticalAlignment = Alignment.Top,
        ) {
            // The symbol lines up with the first text line instead of floating at the block's centre.
            if (leadingIcon != null) {
                Icon(leadingIcon, null, tint = c.iconDefault, modifier = Modifier.padding(top = 2.dp).size(HqIconSize.sm))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(name, style = HqType.rowTitle, color = titleColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                    Text(assigneeText, style = HqType.bodyMedium, color = c.textSecondary)
                    Text("·", style = HqType.bodyMedium, color = c.textMuted)
                    if (overdue) Icon(Icons.Filled.Schedule, null, tint = c.statusDangerFg, modifier = Modifier.size(HqIconSize.xs))
                    Text(dueText, style = HqType.bodyMedium, color = if (overdue) c.statusDangerFg else c.textSecondary)
                }
            }
        }
        if (canComplete) {
            Box(
                Modifier.size(HqSize.target)
                    .clickable(enabled = !completing, role = Role.Button, onClick = onComplete)
                    .semantics { contentDescription = if (completing) "Completing $name" else "Complete $name" },
                contentAlignment = Alignment.Center,
            ) {
                if (completing) {
                    CircularProgressIndicator(Modifier.size(HqIconSize.md), strokeWidth = 2.dp, color = c.textBrand)
                } else {
                    Icon(Icons.Outlined.CheckCircleOutline, null, tint = c.textBrand, modifier = Modifier.size(HqIconSize.md))
                }
            }
            Spacer(Modifier.width(HqSpacing.xs))
        } else {
            Spacer(Modifier.width(HqSpacing.component))
        }
    }
}

/** A single navigation or summary row inside a [HqGroup]: title, optional support line, trailing value and disclosure. */
@Composable
fun HqNavRow(
    title: String,
    modifier: Modifier = Modifier,
    support: String? = null,
    trailing: String? = null,
    leadingIcon: ImageVector? = null,
    showDisclosure: Boolean = true,
    onClick: () -> Unit,
) {
    val c = LocalHqColors.current
    Row(
        modifier.fillMaxWidth().defaultMinSize(minHeight = if (support != null) HqSize.rowTwoLine else HqSize.row)
            .hqPressable(onClick = onClick)
            .padding(horizontal = HqSpacing.component, vertical = HqSpacing.related),
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.related),
        verticalAlignment = if (support != null) Alignment.Top else Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, tint = c.iconDefault, modifier = Modifier.padding(top = if (support != null) 2.dp else 0.dp).size(HqIconSize.sm))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = HqType.rowTitle, color = c.textPrimary)
            if (support != null) Text(support, style = HqType.bodyMedium, color = c.textSecondary)
        }
        if (trailing != null) Text(trailing, style = HqType.bodyMedium, color = c.textSecondary)
        if (showDisclosure) Icon(Icons.Filled.ChevronRight, null, tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
    }
}

/**
 * Illustrated feature panel (design doc 19): text on the start side, bounded art on the end side.
 * Soft selected fill and no outline. Use at most one per screen, where the art has a defined role.
 */
@Composable
fun HqIllustratedPanel(
    art: HqArt,
    modifier: Modifier = Modifier,
    artSize: Dp = 88.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalHqColors.current
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(HqRadius.card)).background(c.selectedBg)
            .padding(start = HqSpacing.card, top = HqSpacing.component, bottom = HqSpacing.component, end = HqSpacing.component),
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.related),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs), content = content)
        HqIllustration(art, Modifier.width(artSize))
    }
}
