package habitiq.app.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqArt
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.components.HqNavRow
import habitiq.app.ui.components.HqRowDivider
import habitiq.app.ui.components.HqSectionHeader
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

data class ManageAttention(val kind: Kind, val title: String, val support: String, val taskId: String? = null) {
    enum class Kind { OverdueTask, Balance, Swaps }
}

/** A short summary line on a tile. [urgent] adds a clock-free warning colour for things like "1 overdue". */
data class TileLine(val text: String, val urgent: Boolean = false)

data class ManageUiModel(
    val flatContext: String,
    val tasksLines: List<TileLine>,
    val expensesLines: List<TileLine>,
    val attention: List<ManageAttention>,
    val activity: List<HomeActivityItem>,
)

/**
 * Manage hub (design doc 19.2): a compact header and two real choices, Tasks and Expenses, then only
 * things that genuinely need attention. Month close and admin tools are deliberately not tiles here.
 */
@Composable
fun ManageContent(
    model: ManageUiModel,
    onOpenTasks: () -> Unit,
    onOpenExpenses: () -> Unit,
    onAttention: (ManageAttention) -> Unit,
    onOpenActivity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalHqColors.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = HqSpacing.screenHorizontal)
            .padding(top = HqSpacing.sm, bottom = HqSpacing.screenEnd),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.group),
    ) {
        Column {
            Text("Manage flat", style = HqType.titleLarge2, color = c.textPrimary)
            if (model.flatContext.isNotBlank()) Text(model.flatContext, style = HqType.bodyMedium, color = c.textSecondary)
        }

        // Two columns only when both tiles can hold their text; otherwise full-width rows.
        val fontScale = LocalDensity.current.fontScale
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val twoColumns = maxWidth >= 320.dp && fontScale <= 1.3f
            if (twoColumns) {
                Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(HqSpacing.related)) {
                    ManageTile("Tasks", model.tasksLines, HqArt.Checklist, onOpenTasks, Modifier.weight(1f))
                    ManageTile("Expenses", model.expensesLines, HqArt.Receipt, onOpenExpenses, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.related)) {
                    ManageRowTile("Tasks", model.tasksLines, HqArt.Checklist, onOpenTasks)
                    ManageRowTile("Expenses", model.expensesLines, HqArt.Receipt, onOpenExpenses)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqSectionHeader("Needs your attention")
            HqGroup {
                if (model.attention.isEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().padding(HqSpacing.component),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = c.statusSuccessFg, modifier = Modifier.size(HqIconSize.md))
                        Text("You're all caught up.", style = HqType.bodyLarge, color = c.textPrimary)
                    }
                }
                model.attention.forEachIndexed { index, item ->
                    if (index > 0) HqRowDivider()
                    HqNavRow(title = item.title, support = item.support, onClick = { onAttention(item) })
                }
            }
        }

        if (model.activity.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.related)) {
                HqSectionHeader("Recent activity", action = "View all", onAction = onOpenActivity)
                model.activity.forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(item.text, style = HqType.bodyLarge, color = c.textPrimary)
                        Text(item.time, style = HqType.labelSmall, color = c.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun tileModifier(onClick: () -> Unit): Modifier {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(HqRadius.card)
    return Modifier.clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
        .clickable(role = Role.Button, onClick = onClick)
}

/**
 * Half-width tile: a small vignette with the cue beside it, the title, then short real summary
 * lines. Target about 150dp at default text; it grows with content and the two tiles share a height.
 */
@Composable
private fun ManageTile(title: String, lines: List<TileLine>, art: HqArt, onClick: () -> Unit, modifier: Modifier) {
    val c = LocalHqColors.current
    Column(
        modifier.then(tileModifier(onClick)).fillMaxHeight().padding(HqSpacing.component),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.xs),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            HqIllustration(art, Modifier.width(44.dp))
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
        }
        Text(title, style = HqType.titleSmall2, color = c.textPrimary, modifier = Modifier.padding(top = HqSpacing.xs))
        lines.forEach { line ->
            Text(line.text, style = HqType.bodyMedium, color = if (line.urgent) c.statusDangerFg else c.textSecondary)
        }
    }
}

/** Full-width variant for narrow windows and large text. */
@Composable
private fun ManageRowTile(title: String, lines: List<TileLine>, art: HqArt, onClick: () -> Unit) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().then(tileModifier(onClick)).padding(HqSpacing.component),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.component),
    ) {
        HqIllustration(art, Modifier.width(44.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = HqType.titleSmall2, color = c.textPrimary)
            lines.forEach { line ->
                Text(line.text, style = HqType.bodyMedium, color = if (line.urgent) c.statusDangerFg else c.textSecondary)
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
    }
}
