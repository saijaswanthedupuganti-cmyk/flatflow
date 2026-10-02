package habitiq.app.ui.components

import habitiq.app.ui.theme.hqPressable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Soft tinted tile for a symbol (Figma `.icon-tile`): teal, coral, sand or neutral. */
enum class HqTileTone { Teal, Coral, Sand, Neutral }

@Composable
fun HqIconTile(icon: ImageVector?, tone: HqTileTone, modifier: Modifier = Modifier, size: Int = 42, glyph: String? = null) {
    val c = LocalHqColors.current
    val (bg, fg) = when (tone) {
        HqTileTone.Teal -> c.selectedBg to c.selectedFg
        HqTileTone.Coral -> c.warmBg to Color(0xFFBC5144)
        HqTileTone.Sand -> Color(0xFFF3EADC) to Color(0xFF806039)
        HqTileTone.Neutral -> c.surfaceSubtle to c.textSecondary
    }
    Box(modifier.size(size.dp).clip(RoundedCornerShape((size * 0.31f).dp)).background(bg), contentAlignment = Alignment.Center) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(HqIconSize.md))
        else if (glyph != null) Text(glyph, style = HqType.titleSmall2, color = fg)
    }
}

/** Short capsule label such as "YOU" (Figma `.you-pill`). */
@Composable
fun HqPill(label: String, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    Text(
        label,
        style = HqType.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = c.selectedFg,
        modifier = modifier.clip(RoundedCornerShape(6.dp)).background(c.selectedBg).padding(horizontal = 7.dp, vertical = 5.dp),
    )
}

/**
 * Figma `.task-card`: white card, line border, 18dp radius, soft lift. The completion box sits in its own
 * 48dp target so completing never also opens the detail. [trailing] is the "YOU" pill, an avatar or a chevron.
 */
@Composable
fun HqTaskCard(
    title: String,
    meta: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    canComplete: Boolean = false,
    completing: Boolean = false,
    done: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onOpen: () -> Unit = {},
    onComplete: () -> Unit = {},
) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier.fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = c.textPrimary.copy(alpha = .06f), spotColor = c.textPrimary.copy(alpha = .06f))
            .clip(shape).background(c.surfaceBase).border(BorderStroke(1.dp, c.borderSubtle), shape)
            .defaultMinSize(minHeight = 72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (canComplete) {
            Box(
                Modifier.size(HqSize.target).padding(start = 6.dp)
                    .clickable(enabled = !completing && !done, role = Role.Button, onClick = onComplete)
                    .semantics { contentDescription = if (completing) "Completing $title" else "Complete $title" },
                contentAlignment = Alignment.Center,
            ) {
                if (completing) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = c.textBrand)
                } else {
                    Box(
                        Modifier.size(25.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (done) c.brandTeal else c.surfaceBase)
                            .border(1.5.dp, if (done) c.brandTeal else Color(0xFFAAB7B5), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) { if (done) Icon(HqIcons.Check, null, tint = c.actionPrimaryFg, modifier = Modifier.size(HqIconSize.xs)) }
                }
            }
        } else if (leading != null) {
            Box(Modifier.padding(start = 13.dp)) { leading() }
        }
        Row(
            Modifier.weight(1f).clickable(role = Role.Button, onClick = onOpen)
                .padding(start = if (canComplete) 6.dp else 12.dp, end = 13.dp, top = 13.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.related),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    style = HqType.titleSmall2.copy(fontSize = HqType.rowTitle.fontSize),
                    color = c.textPrimary,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                )
                meta()
            }
            trailing?.invoke()
        }
    }
}

/** Figma `.activity-callout`: coral-tinted card with a tile, a title, a support line and a chevron. */
@Composable
fun HqCalloutCard(
    title: String,
    support: String,
    icon: ImageVector?,
    tone: HqTileTone,
    modifier: Modifier = Modifier,
    glyph: String? = null,
    onClick: () -> Unit,
) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(18.dp)
    val (bg, border) = if (tone == HqTileTone.Coral) Color(0xFFFFFAFA) to Color(0xFFF7D8D3) else c.surfaceBase to c.borderSubtle
    Row(
        modifier.fillMaxWidth().clip(shape).background(bg).border(1.dp, border, shape)
            .hqPressable(onClick = onClick).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HqIconTile(icon, tone, glyph = glyph)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = HqType.rowTitle, color = c.textPrimary)
            Text(support, style = HqType.bodyMedium, color = c.textSecondary)
        }
        Icon(HqIcons.Chevron, null, tint = c.iconDefault, modifier = Modifier.size(HqIconSize.sm))
    }
}

/** One entry of the Figma activity timeline: a round marker, text and time. */
@Composable
fun HqTimelineItem(text: String, time: String, modifier: Modifier = Modifier, money: Boolean = false, icon: ImageVector = HqIcons.Check) {
    val c = LocalHqColors.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(if (money) Color(0xFFF3EADC) else c.selectedBg),
            contentAlignment = Alignment.Center,
        ) {
            if (money) Text("₹", style = HqType.labelMedium, color = Color(0xFF7D613B))
            else Icon(icon, null, tint = c.selectedFg, modifier = Modifier.size(15.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text, style = HqType.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary)
            Text(time, style = HqType.labelSmall, color = c.textMuted)
        }
    }
}
