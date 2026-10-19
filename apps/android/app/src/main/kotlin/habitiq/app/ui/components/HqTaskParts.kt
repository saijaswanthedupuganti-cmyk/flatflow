package habitiq.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import habitiq.app.ui.theme.hqReduceMotion
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
import habitiq.app.ui.theme.HqShapes
import androidx.compose.ui.platform.LocalDensity
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
        Text(left, style = HqType.labelMedium, color = c.textSecondary, fontWeight = FontWeight.SemiBold)
        if (right != null) Text(right, style = HqType.labelMedium, color = c.textSecondary, fontWeight = FontWeight.SemiBold)
    }
}

enum class HqBadgeTone { Warning, Success, Info, Neutral, Danger }

/** Figma `.status-badge`: tiny uppercase capsule with a soft fill. */
@Composable
fun HqBadge(label: String, tone: HqBadgeTone, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val (bg, fg) = when (tone) {
        HqBadgeTone.Warning -> c.statusWarningBg to c.statusWarningFg
        HqBadgeTone.Success -> c.selectedBg to c.selectedFg
        HqBadgeTone.Info -> c.statusInfoBg to c.statusInfoFg
        HqBadgeTone.Neutral -> c.surfaceSubtle to c.textSecondary
        HqBadgeTone.Danger -> c.statusDangerBg to c.statusDangerFg
    }
    Text(
        label.replaceFirstChar { it.uppercase() }, style = HqType.labelSmall, color = fg, fontWeight = FontWeight.SemiBold,
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
    val shape = HqShapes.card
    val edge = c.statusDangerFg
    // The decorative chore motif only shows at normal text sizes; at large text it gives its space to the words.
    val largeText = LocalDensity.current.fontScale > 1.15f
    val motif = if (largeText) null else hqTaskMotifFor(title)
    Box(
        modifier.fillMaxWidth()

            .clip(shape)
            .background(c.surfaceBase)
            .border(BorderStroke(1.dp, c.borderSubtle), shape)
            .then(if (overdue) Modifier.drawBehind { drawLine(edge, Offset(1.5.dp.toPx(), 12.dp.toPx()), Offset(1.5.dp.toPx(), size.height - 12.dp.toPx()), strokeWidth = 3.dp.toPx()) } else Modifier)
            .clickable(role = Role.Button, onClick = onOpen)
            .defaultMinSize(minHeight = 94.dp),
    ) {
        if (motif != null) {
            Icon(
                hqTaskMotifVector(motif), null, tint = c.textBrand.copy(alpha = .22f),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 62.dp).size(66.dp),
            )
        }
        Row(Modifier.padding(horizontal = 6.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(HqSize.target).then(
                    if (canComplete) {
                        Modifier.clickable(enabled = !completing && !done, role = Role.Button, onClick = onComplete)
                            .semantics { contentDescription = "Complete $title" }
                    } else {
                        Modifier.clearAndSetSemantics {}
                    },
                ),
                contentAlignment = Alignment.Center,
            ) {
                if (completing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = c.textBrand)
                else Box(
                    Modifier.size(25.dp).clip(RoundedCornerShape(8.dp)).background(if (done) c.brandTeal else c.surfaceBase)
                        .border(1.5.dp, if (done) c.brandTeal else c.borderControl, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) { if (done) Icon(HqIcons.Check, null, tint = c.actionPrimaryFg, modifier = Modifier.size(HqIconSize.xs)) }
            }
            Column(Modifier.weight(1f).padding(start = 4.dp, end = if (motif != null) 52.dp else 4.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = HqType.rowTitle, color = if (quiet) c.textSecondary else c.textPrimary)
                Text(due, style = HqType.bodyMedium, color = c.textSecondary)
                if (badge != null) HqBadge(badge, if (overdue) HqBadgeTone.Danger else HqBadgeTone.Warning, Modifier.padding(top = 4.dp))
                if (meta != null) {
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (metaIcon != null) Icon(metaIcon, null, tint = c.textSecondary, modifier = Modifier.size(15.dp))
                        Text(meta, style = HqType.labelSmall, color = c.textSecondary)
                    }
                }
            }
            Column(Modifier.padding(end = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HqAvatar(assigneeName, size = HqAvatarSize.SM, tone = hqToneFor(assigneeName, assigneeIsMe))
                Text(assigneeLabel, style = HqType.labelSmall, color = c.textSecondary, fontWeight = FontWeight.SemiBold, maxLines = 1)
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
    BoxWithConstraints(modifier.fillMaxWidth().heightIn(min = 58.dp).clip(track).background(c.surfaceSubtle).padding(4.dp).selectableGroup()) {
        val cell = maxWidth / options.size
        val reduceMotion = hqReduceMotion()
        val x by animateDpAsState(
            cell * selectedIndex,
            animationSpec = if (reduceMotion) snap() else spring(visibilityThreshold = Dp.VisibilityThreshold),
            label = "manageSwitch",
        )
        // matchParentSize: the track can grow with font scale (heightIn), so the indicator follows its height.
        Box(
            Modifier.matchParentSize().wrapContentWidth(Alignment.Start).offset(x = x).width(cell)
                .shadow(6.dp, RoundedCornerShape(14.dp), ambientColor = c.textPrimary.copy(alpha = .1f), spotColor = c.textPrimary.copy(alpha = .1f))
                .clip(RoundedCornerShape(14.dp)).background(c.surfaceBase),
        )
        Row(Modifier.fillMaxWidth().heightIn(min = 50.dp).height(IntrinsicSize.Min)) {
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
