package habitiq.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Two (or a few) mutually exclusive scopes on one shared track, e.g. My tasks / All tasks. Cells
 * share the width equally because it is a genuine equal-choice control (design doc 10.4). Each
 * cell is a full 48dp target; the selected state is a raised pill plus heavier text and the
 * selected semantics, never colour alone.
 */
@Composable
fun HqSegmentedControl(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    // Figma `.segmented`: #eff3f2 track, 5dp padding, 3dp gaps, 15dp radius; active cell is a white 11dp pill with a soft lift.
    val track = RoundedCornerShape(15.dp)
    val cell = RoundedCornerShape(11.dp)
    Row(
        modifier.fillMaxWidth().clip(track).background(Color(0xFFEFF3F2)).padding(5.dp).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                Modifier.weight(1f).defaultMinSize(minHeight = HqSize.target)
                    .then(if (selected) Modifier.shadow(4.dp, cell, ambientColor = c.textPrimary.copy(alpha = .09f), spotColor = c.textPrimary.copy(alpha = .09f)) else Modifier)
                    .clip(cell)
                    .background(if (selected) c.surfaceBase else Color.Transparent)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = HqType.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) c.textPrimary else c.textSecondary,
                    modifier = Modifier.padding(horizontal = HqSpacing.sm),
                )
            }
        }
    }
}

/**
 * Secondary scope tabs (e.g. My tasks / All tasks) that sit under a primary [HqSegmentedControl].
 * Lighter than a second segmented track so the two levels never look identical: text tabs with a
 * count pill and a 2dp brand underline on the selected tab, over a hairline divider.
 */
@Composable
fun HqScopeTabs(options: List<Pair<String, Int?>>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    Box(modifier.fillMaxWidth()) {
        Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(1.dp).background(c.borderSubtle))
        Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            options.forEachIndexed { index, (label, count) ->
                val selected = index == selectedIndex
                Column(
                    Modifier.width(IntrinsicSize.Max)
                        .defaultMinSize(minHeight = HqSize.target)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) }),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Row(
                        Modifier.padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Text(
                            label,
                            style = HqType.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) c.textPrimary else c.textSecondary,
                            maxLines = 1,
                        )
                        if (count != null) {
                            Box(
                                Modifier.clip(RoundedCornerShape(HqRadius.pill))
                                    .background(if (selected) c.selectedBg else c.surfaceSubtle)
                                    .padding(horizontal = 7.dp, vertical = 1.dp),
                            ) {
                                Text("$count", style = HqType.labelSmall, fontWeight = FontWeight.Bold, color = if (selected) c.textBrand else c.textMuted)
                            }
                        }
                    }
                    Box(
                        Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp))
                            .background(if (selected) c.brandTeal else Color.Transparent),
                    )
                }
            }
        }
    }
}

/**
 * Figma `.toggle`: 46x27 track (off #cbd5d2, on brand), 21dp white thumb. The track is exposed as a
 * Switch with a 48dp touch box, so state is announced rather than colour-only.
 */
@Composable
fun HqSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val c = LocalHqColors.current
    val thumbX by androidx.compose.animation.core.animateDpAsState(if (checked) 19.dp else 0.dp, label = "switchThumb")
    Box(
        modifier
            .defaultMinSize(minWidth = HqSize.target, minHeight = HqSize.target)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(width = 46.dp, height = 27.dp).clip(RoundedCornerShape(HqRadius.pill))
                .background(if (checked) c.actionPrimaryBg else Color(0xFFCBD5D2)).padding(3.dp),
        ) {
            Box(Modifier.offset(x = thumbX).size(21.dp).clip(CircleShape).background(Color.White))
        }
    }
}

/** A short read-only attribute such as "Quiet evenings". Content-width, no button role, no target. */
@Composable
fun HqTag(label: String, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    Text(
        label,
        style = HqType.labelSmall,
        color = c.textSecondary,
        modifier = modifier.clip(RoundedCornerShape(HqRadius.small)).background(c.surfaceSubtle)
            .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs),
    )
}

/**
 * Read-only tags that share each line and wrap (design doc 10.4): start-aligned, 8dp gaps, never one
 * tag per line. With [maxRows] set, extra tags collapse behind a working "+N more" that calls
 * [onMore], so the complete set is always reachable. The indicator is guaranteed to fit on the last
 * visible row; tags are dropped (and the count updated) until it does.
 */
@Composable
fun HqTagFlow(tags: List<String>, modifier: Modifier = Modifier, maxRows: Int = Int.MAX_VALUE, onMore: (() -> Unit)? = null) {
    if (tags.isEmpty()) return
    val c = LocalHqColors.current
    SubcomposeLayout(modifier.fillMaxWidth()) { constraints ->
        val gapX = HqSpacing.sm.roundToPx()
        val gapY = HqSpacing.sm.roundToPx()
        val maxW = constraints.maxWidth
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val items = tags.mapIndexed { i, label -> subcompose("tag$i") { HqTag(label) }.first().measure(loose) }

        // Greedy rows: each row is a list of indices into items.
        fun pack(count: Int): List<List<Int>> {
            val packed = ArrayList<ArrayList<Int>>()
            var x = 0
            for (i in 0 until count) {
                val w = items[i].width
                if (packed.isEmpty() || (packed.last().isNotEmpty() && x + gapX + w > maxW)) { packed.add(ArrayList()); x = 0 }
                if (packed.last().isNotEmpty()) x += gapX
                packed.last().add(i)
                x += w
            }
            return packed
        }

        var shown = items.size
        var rows: List<List<Int>> = pack(shown)
        var indicator: androidx.compose.ui.layout.Placeable? = null
        if (rows.size > maxRows) {
            rows = pack(shown).take(maxRows)
            shown = rows.sumOf { it.size }
            while (true) {
                val hidden = items.size - shown
                val ind = subcompose("more$hidden") {
                    Text(
                        "+$hidden more",
                        style = HqType.labelSmall,
                        color = c.textBrand,
                        modifier = Modifier
                            .defaultMinSize(minHeight = HqSize.target)
                            .then(if (onMore != null) Modifier.clickable(role = Role.Button, onClick = onMore) else Modifier)
                            .padding(horizontal = HqSpacing.xs)
                            .wrapContentHeight(Alignment.CenterVertically),
                    )
                }.first().measure(loose)
                val last = rows.last()
                val used = last.sumOf { items[it].width } + gapX * (last.size - 1).coerceAtLeast(0)
                val fits = last.isEmpty() || used + gapX + ind.width <= maxW
                if (fits || last.isEmpty() && rows.size == 1) { indicator = ind; break }
                if (last.size == 1) { rows = rows.dropLast(1) + listOf(emptyList()); shown -= 1; continue }
                rows = rows.dropLast(1) + listOf(last.dropLast(1))
                shown -= 1
            }
        }

        // Positions
        val rowHeights = rows.mapIndexed { r, row ->
            var h = row.maxOfOrNull { items[it].height } ?: 0
            if (indicator != null && r == rows.lastIndex) h = maxOf(h, indicator.height)
            h
        }
        val totalH = rowHeights.sum() + gapY * (rows.size - 1).coerceAtLeast(0)
        layout(maxW, totalH.coerceAtLeast(0)) {
            var y = 0
            rows.forEachIndexed { r, row ->
                var x = 0
                row.forEach { idx ->
                    val p = items[idx]
                    p.placeRelative(x, y + (rowHeights[r] - p.height) / 2)
                    x += p.width + gapX
                }
                if (indicator != null && r == rows.lastIndex) {
                    indicator.placeRelative(x, y + (rowHeights[r] - indicator.height) / 2)
                }
                y += rowHeights[r] + gapY
            }
        }
    }
}

/**
 * Filter chips that share each line and wrap. [HqChip] already reserves its own 48dp target box, so
 * neighbours never overlap; an 8dp gap separates the boxes (design doc 10.4).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HqChipFlow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
    ) { content() }
}

/**
 * A selected filter the person can remove. Intrinsic width inside a 48dp target; the whole chip is the
 * remove action and is announced as "Remove filter ...", so the label is never clipped or abbreviated.
 */
@Composable
fun HqRemovableChip(label: String, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(HqRadius.pill)
    Box(
        modifier.defaultMinSize(minHeight = HqSize.target)
            .clickable(role = Role.Button, onClick = onRemove)
            .semantics { contentDescription = "Remove filter $label" },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.defaultMinSize(minHeight = HqSize.chipVisual).clip(shape).background(c.selectedBg)
                .border(2.dp, c.selectedBorder, shape).padding(start = HqSpacing.md, end = HqSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs),
        ) {
            Text(label, style = HqType.labelMedium, color = c.selectedFg)
            Icon(androidx.compose.material.icons.Icons.Filled.Close, null, tint = c.selectedFg, modifier = Modifier.size(16.dp))
        }
    }
}
