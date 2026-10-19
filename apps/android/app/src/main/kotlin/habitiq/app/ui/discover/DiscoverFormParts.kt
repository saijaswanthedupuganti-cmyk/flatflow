package habitiq.app.ui.discover

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqChipFlow
import habitiq.app.ui.components.HqChipKind
import habitiq.app.ui.components.HqInlineInfo
import habitiq.app.ui.components.HqTag
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HqMotion
import habitiq.app.ui.theme.HqShadow
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqPressable
import habitiq.app.ui.theme.hqShadow
import habitiq.app.ui.theme.rememberHqTransitions

/**
 * One wizard frame for every Discover post (List a room, I'm looking for a room):
 * back top-left, "Step N of M" + progress (goal gradient), one topic per step, and ONE full-width
 * primary pinned at the bottom in the thumb zone, lifted above the keyboard.
 * [content] receives the step being drawn, so the outgoing step keeps its own content while it animates out.
 */
@Composable
internal fun DiscoverWizardScaffold(
    title: String,
    step: Int,
    stepCount: Int,
    stepTitle: (Int) -> String,
    onBack: () -> Unit,
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    stepSubtitle: (Int) -> String? = { null },
    loading: Boolean = false,
    bottomNote: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.(Int) -> Unit,
) {
    val c = LocalHqColors.current
    val transitions = rememberHqTransitions()
    val progress by animateFloatAsState((step + 1f) / stepCount, HqMotion.layoutSpring(), label = "wizardProgress")
    Column(modifier.fillMaxSize().background(c.canvas)) {
        HqAppBar(title = title, onBack = onBack)
        Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal)) {
            Text(
                "Step ${step + 1} of $stepCount",
                style = HqType.labelMedium, color = c.textSecondary,
            )
            Spacer(Modifier.height(HqSpacing.sm))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().clip(HqShapes.pill).semantics { contentDescription = "Step ${step + 1} of $stepCount" },
                color = c.actionPrimaryBg,
                trackColor = c.surfaceSubtle,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                if (targetState > initialState) transitions.pushEnter togetherWith transitions.pushExit
                else transitions.popEnter togetherWith transitions.popExit
            },
            label = "wizardStep",
        ) { shown ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = HqSpacing.screenHorizontal)
                    .padding(top = HqSpacing.xxl, bottom = HqSpacing.xxxl),
            ) {
                Text(stepTitle(shown), style = HqType.titleMedium2, color = c.textPrimary, modifier = Modifier.semantics { heading() })
                stepSubtitle(shown)?.let {
                    Spacer(Modifier.height(HqSpacing.xs))
                    Text(it, style = HqType.bodyMedium, color = c.textSecondary)
                }
                Spacer(Modifier.height(HqSpacing.xxl))
                content(shown)
            }
        }
        // Sticky action bar: one primary, thumb zone, above the keyboard.
        Column(
            Modifier.fillMaxWidth().background(c.canvas).imePadding().navigationBarsPadding(),
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
            Column(
                Modifier.padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.md),
                verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
            ) {
                bottomNote?.invoke()
                HqButton(text = primaryLabel, onClick = onPrimary, loading = loading, enabled = true)
            }
        }
    }
}

/** A labelled question inside a step. Space groups it; no dividers. */
@Composable
internal fun FormQuestion(label: String, modifier: Modifier = Modifier, helper: String? = null, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalHqColors.current
    Column(modifier.fillMaxWidth()) {
        Text(label, style = HqType.labelMedium, color = c.textPrimary)
        if (helper != null) {
            Text(helper, style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = HqSpacing.xs / 2))
        }
        Spacer(Modifier.height(HqSpacing.xs))
        content()
    }
}

/** Gap between questions on a step (form field gap, design system: 20-24). */
@Composable
internal fun QuestionGap() = Spacer(Modifier.height(HqSpacing.xxl))

/** One-of-a-few choice chips that wrap; at most five options per question (Hick). */
@Composable
internal fun ChoiceChips(
    options: List<Pair<String, String>>,
    isSelected: (String) -> Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    HqChipFlow(modifier.selectableGroup()) {
        options.forEach { (key, label) ->
            HqChip(label = label, selected = isSelected(key), onClick = { onSelect(key) }, kind = HqChipKind.Choice)
        }
    }
}

/** A group of multi-select tags inside a [TagPicker]. */
internal class TagGroup(val title: String, val options: List<String>, val selected: List<String>, val onToggle: (String) -> Unit)

/**
 * Multi-select tags with a cap: the most common [collapsedPerGroup] per group are shown, "Show more"
 * reveals the rest, and a counter says how many are picked. Tapping past the cap explains it instead
 * of silently failing; nothing is disabled without a reason.
 */
@Composable
internal fun TagPicker(groups: List<TagGroup>, max: Int, collapsedPerGroup: Int = 4) {
    val c = LocalHqColors.current
    var expanded by rememberSaveable { mutableStateOf(false) }
    var overLimit by remember { mutableStateOf(false) }
    val total = groups.sumOf { it.selected.size }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Pick up to $max", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.weight(1f))
        Text(
            "$total of $max",
            style = HqType.labelMedium, color = if (total > 0) c.textBrand else c.textSecondary,
            modifier = Modifier.semantics { contentDescription = "$total of $max selected" },
        )
    }
    groups.forEach { group ->
        // Selected values that are not in the list (older posts) stay visible so they can be removed.
        val all = group.options + group.selected.filter { s -> group.options.none { it.equals(s, ignoreCase = true) } }
        val visible = if (expanded) all else all.filterIndexed { i, tag -> i < collapsedPerGroup || group.selected.any { it.equals(tag, true) } }
        Spacer(Modifier.height(HqSpacing.lg))
        Text(group.title, style = HqType.labelMedium, color = c.textPrimary)
        Spacer(Modifier.height(HqSpacing.xs))
        HqChipFlow {
            visible.forEach { tag ->
                val on = group.selected.any { it.equals(tag, ignoreCase = true) }
                HqChip(label = tag, selected = on, onClick = {
                    if (!on && total >= max) {
                        overLimit = true
                    } else {
                        overLimit = false
                        group.onToggle(tag)
                    }
                })
            }
        }
    }
    val hidden = groups.sumOf { g -> (g.options.size - collapsedPerGroup).coerceAtLeast(0) }
    if (hidden > 0) {
        Spacer(Modifier.height(HqSpacing.xs))
        HqTextButton(text = if (expanded) "Show fewer" else "Show $hidden more", onClick = { expanded = !expanded })
    }
    AnimatedVisibility(overLimit, enter = rememberHqTransitions().expandEnter, exit = rememberHqTransitions().collapseExit) {
        HqInlineInfo("You can pick up to $max. Remove one to add another.", Modifier.padding(top = HqSpacing.sm))
    }
}

/**
 * Read-only tags in ONE row that never wraps: at most [max] tags, then a "+N" tag of the same height.
 * Tags that don't fit the width (large fonts) fold into the "+N" count, so the row stays one line.
 */
@Composable
internal fun OneRowTags(tags: List<String>, modifier: Modifier = Modifier, max: Int = 3) {
    if (tags.isEmpty()) return
    SubcomposeLayout(modifier.fillMaxWidth().semantics { contentDescription = "Tags: ${tags.joinToString(", ")}" }) { constraints ->
        val gap = HqSpacing.sm.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val maxW = constraints.maxWidth
        val candidates = tags.take(max).mapIndexed { i, t -> subcompose("t$i") { HqTag(t) }.first().measure(loose) }
        fun moreTag(n: Int) = subcompose("m$n") { HqTag("+$n") }.first().measure(loose)
        var shown = candidates.size
        var more: androidx.compose.ui.layout.Placeable? = null
        while (true) {
            val hidden = tags.size - shown
            val m = if (hidden > 0) moreTag(hidden) else null
            val width = candidates.take(shown).sumOf { it.width } + gap * (shown - 1).coerceAtLeast(0) +
                (m?.let { gap + it.width } ?: 0)
            if (width <= maxW || shown == 0) { more = m; break }
            shown -= 1
        }
        val placed = candidates.take(shown)
        val h = (placed.map { it.height } + listOfNotNull(more?.height)).maxOrNull() ?: 0
        layout(maxW, h) {
            var x = 0
            placed.forEach { p -> p.placeRelative(x, (h - p.height) / 2); x += p.width + gap }
            more?.placeRelative(x, (h - more.height) / 2)
        }
    }
}

/**
 * The one card shell for Discover results (flat and flatmate): 20dp radius, floating shadow, 1dp
 * subtle border, optional [media] on top (clipped by the card's rounded top), 16dp body, whole card
 * tappable with an explicit "View" action label for TalkBack.
 */
@Composable
internal fun DiscoverCardShell(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    clickLabel: String = "View",
    media: (@Composable () -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalHqColors.current
    val shape = HqShapes.card
    Column(
        modifier.fillMaxWidth()
            .hqShadow(HqShadow.Floating, shape)
            .clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
            .then(if (onClick != null) Modifier.hqPressable(onClick = onClick) else Modifier)
            .semantics { if (onClick != null) contentDescription = clickLabel },
    ) {
        media?.invoke()
        Column(Modifier.padding(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs + HqSpacing.xs / 2)) { body() }
    }
}

/** Title (wraps to two lines) with an amount right-aligned on the same line, tabular figures. */
@Composable
internal fun TitleAmountRow(title: String, amount: String?, amountUnit: String?) {
    val c = LocalHqColors.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HqSpacing.md), verticalAlignment = Alignment.Top) {
        Text(title, style = HqType.titleSmall2, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (amount != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(amount, style = HqType.amountRow, color = c.textPrimary)
                if (amountUnit != null) Text(amountUnit, style = HqType.labelSmall, color = c.textSecondary, modifier = Modifier.padding(start = HqSpacing.xs / 2, bottom = HqSpacing.xs / 2))
            }
        }
    }
}

/** A small icon + one line of quiet text (area, trust). */
@Composable
internal fun IconLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: androidx.compose.ui.graphics.Color? = null, style: androidx.compose.ui.text.TextStyle = HqType.bodyMedium) {
    val c = LocalHqColors.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs + HqSpacing.xs / 2)) {
        Icon(icon, null, tint = tint ?: c.iconDefault, modifier = Modifier.size(habitiq.app.ui.theme.HqIconSize.xs))
        Text(text, style = style, color = c.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
