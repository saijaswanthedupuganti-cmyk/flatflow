package habitiq.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Decorative task illustrations from the Figma `TaskMotif`; chosen from the task name, none when nothing fits. */
enum class HqTaskMotif { Bin, Plant, Clean }

fun hqTaskMotifFor(name: String): HqTaskMotif? {
    val n = name.lowercase()
    return when {
        listOf("garbage", "trash", "bin", "waste", "rubbish").any { it in n } -> HqTaskMotif.Bin
        listOf("plant", "water", "garden").any { it in n } -> HqTaskMotif.Plant
        listOf("clean", "kitchen", "bathroom", "sweep", "mop", "dust", "laundry").any { it in n } -> HqTaskMotif.Clean
        else -> null
    }
}

private fun motifVector(motif: HqTaskMotif): ImageVector {
    val b = ImageVector.Builder("Hq.Motif.$motif", 92.dp, 92.dp, 92f, 92f)
    fun fill(d: String, alpha: Float) = b.addPath(addPathNodes(d), fill = SolidColor(Color.Black), fillAlpha = alpha)
    fun stroke(d: String, w: Float, alpha: Float = 1f, fillAlpha: Float? = null) = b.addPath(
        addPathNodes(d), fill = fillAlpha?.let { SolidColor(Color.Black) }, fillAlpha = fillAlpha ?: 1f,
        stroke = SolidColor(Color.Black), strokeAlpha = alpha, strokeLineWidth = w, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
    )
    fun circle(cx: Float, cy: Float, r: Float) = "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"
    when (motif) {
        HqTaskMotif.Bin -> {
            fill(circle(48f, 47f, 38f), .06f)
            fill("M34 33h29l-2.5 39h-24L34 33Z", .12f)
            stroke("M31 31h35M40 31l2-7h13l2 7M43 42v20m9-20v20", 3f)
            stroke("m23 57 5 2-3 5", 2.5f, .55f)
        }
        HqTaskMotif.Plant -> {
            fill(circle(48f, 48f, 37f), .06f)
            stroke("M48 69V38m0 14c-10 0-17-6-18-16 10-1 17 5 18 16Zm0 5c10 0 17-6 18-16-10-1-17 5-18 16Z", 2f, fillAlpha = .2f)
            fill("M35 67h27l-4 11H39l-4-11Z", .17f)
            fill("M72 25c0 5-4 8-7 8 0-4 2-7 7-8Z", .45f)
        }
        HqTaskMotif.Clean -> {
            fill(circle(47f, 48f, 37f), .055f)
            fill("M42 35h18l5 39H38l4-39Z", .1f)
            stroke("M47 35v-8h13l6 5M41 45h21M29 30v10m-5-5h10M69 20v8m-4-4h8", 2.5f)
            fill("M53 53c5 4 5 9 0 13-5-4-5-9 0-13Z", .28f)
        }
    }
    return b.build()
}

private val motifCache = HashMap<HqTaskMotif, ImageVector>()
fun hqTaskMotifVector(m: HqTaskMotif): ImageVector = motifCache.getOrPut(m) { motifVector(m) }

/** Figma `.list-heading`: small tracked caps on the left, an optional count on the right. */
@Composable
fun HqListHeading(left: String, modifier: Modifier = Modifier, right: String? = null) {
    val c = LocalHqColors.current
    Row(modifier.fillMaxWidth().padding(top = 21.dp, bottom = 9.dp, start = 3.dp, end = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(left.uppercase(), style = HqType.labelSmall, color = c.textMuted, fontWeight = FontWeight.ExtraBold)
        if (right != null) Text(right.uppercase(), style = HqType.labelSmall, color = c.textMuted, fontWeight = FontWeight.ExtraBold)
    }
}

enum class HqBadgeTone { Warning, Success, Info, Neutral }

/** Figma `.status-badge`: tiny uppercase capsule with a soft fill. */
@Composable
fun HqBadge(label: String, tone: HqBadgeTone, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val (bg, fg) = when (tone) {
        HqBadgeTone.Warning -> c.statusWarningBg to c.statusWarningFg
        HqBadgeTone.Success -> c.selectedBg to c.selectedFg
        HqBadgeTone.Info -> Color(0xFFEDF5FA) to Color(0xFF2F6784)
        HqBadgeTone.Neutral -> c.surfaceSubtle to c.textSecondary
    }
    Text(
        label.uppercase(), style = HqType.labelSmall, color = fg, fontWeight = FontWeight.ExtraBold,
        modifier = modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 7.dp, vertical = 4.dp),
    )
}

/** Avatar tone from a name, so the same flatmate keeps the same colour everywhere. */
fun hqToneFor(name: String, isMe: Boolean): HqAvatarTone =
    if (isMe) HqAvatarTone.Teal else if (name.hashCode() % 2 == 0) HqAvatarTone.Sand else HqAvatarTone.Coral

/**
 * Figma `.large-task`: completion box, title and due line, optional badge or meta line, a faint motif and
 * the assignee. Overdue gets a coral left edge and a warm wash. The box is its own 48dp target.
 */
@Composable
fun HqLargeTaskCard(
    title: String,
    due: String,
    assigneeName: String,
    assigneeLabel: String,
    modifier: Modifier = Modifier,
    assigneeIsMe: Boolean = false,
    badge: String? = null,
    meta: String? = null,
    metaIcon: ImageVector? = null,
    overdue: Boolean = false,
    quiet: Boolean = false,
    done: Boolean = false,
    canComplete: Boolean = false,
    completing: Boolean = false,
    onOpen: () -> Unit,
    onComplete: () -> Unit = {},
) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(19.dp)
    val edge = Color(0xFFFF6B5A)
    val motif = hqTaskMotifFor(title)
    Box(
        modifier.fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = c.textPrimary.copy(alpha = .06f), spotColor = c.textPrimary.copy(alpha = .06f))
            .clip(shape)
            .background(if (overdue) Brush.horizontalGradient(0f to Color(0xFFFFFAF6), .68f to c.surfaceBase) else SolidColor(c.surfaceBase))
            .border(BorderStroke(1.dp, Color(0xFFE1E8E6)), shape)
            .then(if (overdue) Modifier.drawBehind { drawLine(edge, Offset(1.5.dp.toPx(), 12.dp.toPx()), Offset(1.5.dp.toPx(), size.height - 12.dp.toPx()), strokeWidth = 3.dp.toPx()) } else Modifier)
            .clickable(role = Role.Button, onClick = onOpen)
            .defaultMinSize(minHeight = 94.dp),
    ) {
        if (motif != null) {
            Icon(
                hqTaskMotifVector(motif), null, tint = Color(0xFF7BAEA7).copy(alpha = .55f),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 62.dp).size(66.dp),
            )
        }
        Row(Modifier.padding(horizontal = 6.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(HqSize.target).clickable(enabled = canComplete && !completing && !done, role = Role.Button, onClick = onComplete)
                    .semantics { contentDescription = if (canComplete) "Complete $title" else "" },
                contentAlignment = Alignment.Center,
            ) {
                if (completing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = c.textBrand)
                else Box(
                    Modifier.size(25.dp).clip(RoundedCornerShape(8.dp)).background(if (done) c.brandTeal else c.surfaceBase)
                        .border(1.5.dp, if (done) c.brandTeal else Color(0xFFAAB7B5), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) { if (done) Icon(HqIcons.Check, null, tint = c.actionPrimaryFg, modifier = Modifier.size(HqIconSize.xs)) }
            }
            Column(Modifier.weight(1f).padding(start = 4.dp, end = if (motif != null) 52.dp else 4.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = HqType.rowTitle, color = if (quiet) c.textSecondary else c.textPrimary)
                Text(due, style = HqType.bodyMedium, color = c.textSecondary)
                if (badge != null) HqBadge(badge, HqBadgeTone.Warning, Modifier.padding(top = 4.dp))
                if (meta != null) {
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (metaIcon != null) Icon(metaIcon, null, tint = c.textSecondary, modifier = Modifier.size(15.dp))
                        Text(meta, style = HqType.labelSmall, color = c.textSecondary)
                    }
                }
            }
            Column(Modifier.padding(end = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HqAvatar(assigneeName, size = HqAvatarSize.SM, tone = hqToneFor(assigneeName, assigneeIsMe))
                Text(assigneeLabel.uppercase(), style = HqType.labelSmall, color = c.textSecondary, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            }
        }
    }
}

/**
 * Figma `.manage-switch`: a 58dp track with a white sliding indicator behind two labelled halves. Used
 * to flip Manage between Tasks and Expenses without leaving the screen.
 */
@Composable
fun HqManageSwitch(options: List<Pair<String, ImageVector>>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val track = RoundedCornerShape(18.dp)
    BoxWithConstraints(modifier.fillMaxWidth().height(58.dp).clip(track).background(Color(0xFFEDF3F1)).padding(4.dp).selectableGroup()) {
        val cell = maxWidth / options.size
        val x by animateDpAsState(cell * selectedIndex, label = "manageSwitch")
        Box(
            Modifier.offset(x = x).width(cell).fillMaxHeight()
                .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = c.textPrimary.copy(alpha = .1f), spotColor = c.textPrimary.copy(alpha = .1f))
                .clip(RoundedCornerShape(14.dp)).background(c.surfaceBase),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { i, (label, icon) ->
                val selected = i == selectedIndex
                Row(
                    Modifier.weight(1f).fillMaxHeight().selectable(selected = selected, role = Role.Tab, onClick = { onSelect(i) }),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon, null, tint = if (selected) c.textPrimary else c.textMuted, modifier = Modifier.size(18.dp))
                    Text(
                        label, style = HqType.labelMedium, fontWeight = FontWeight.Bold,
                        color = if (selected) c.textPrimary else c.textMuted, modifier = Modifier.padding(start = 7.dp),
                    )
                }
            }
        }
    }
}
