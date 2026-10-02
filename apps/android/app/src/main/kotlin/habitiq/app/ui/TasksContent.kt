package habitiq.app.ui

import habitiq.app.ui.components.HqLargeTaskCard
import habitiq.app.ui.components.HqListHeading
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqCalloutCard
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqArt
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.components.HqNavRow
import habitiq.app.ui.components.HqRowDivider
import habitiq.app.ui.components.HqSegmentedControl
import habitiq.app.ui.components.HqTaskRow
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

enum class TaskScope { Mine, All }

data class TaskListItem(
    val id: String,
    val name: String,
    val assigneeText: String,
    val dueText: String,
    val overdue: Boolean,
    val completed: Boolean,
    val canComplete: Boolean,
    val kind: HomeTaskKind = HomeTaskKind.Rotating,
)

data class TaskSection(val title: String, val items: List<TaskListItem>, val quiet: Boolean = false)

data class TasksUiModel(
    val scope: TaskScope,
    val overdueOnly: Boolean,
    val mineCount: Int,
    val allCount: Int,
    val overdueCount: Int,
    val away: Boolean,
    val pendingSwapsForMe: Int,
    val isAdmin: Boolean,
    val sections: List<TaskSection>,
)

/**
 * Tasks, following the Figma Make `Tasks` / Manage tasks panel: a summary line, My/All segmented control,
 * tracked-caps list headings and large task cards. Overdue stays a filter; away and swap entries are
 * callout cards. [header] renders at the top of the scrolling column (Manage puts its title and switch there).
 */
@Composable
fun TasksContent(
    model: TasksUiModel,
    completingTaskId: String?,
    onScope: (TaskScope) -> Unit,
    onToggleOverdue: () -> Unit,
    onOpenAway: () -> Unit,
    onReviewSwaps: () -> Unit,
    onOpenTask: (String) -> Unit,
    onCompleteTask: (String) -> Unit,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
) {
    val c = LocalHqColors.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = HqSpacing.screenHorizontal)
            .padding(top = HqSpacing.sm, bottom = HqSpacing.screenEnd),
    ) {
        header()

        val toDo = if (model.scope == TaskScope.Mine) model.mineCount else model.allCount
        Row(Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 6.dp, bottom = 23.dp), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(if (model.scope == TaskScope.Mine) "YOUR TASKS" else "ALL TASKS", style = HqType.labelSmall, color = c.textMuted, fontWeight = FontWeight.ExtraBold)
                Text("$toDo to do", style = HqType.display.copy(fontSize = 29.sp), color = c.textPrimary)
            }
            if (model.overdueCount > 0 || model.overdueOnly) {
                HqChip(label = "${model.overdueCount} overdue", selected = model.overdueOnly, onClick = onToggleOverdue)
            }
        }

        HqSegmentedControl(
            options = listOf("My Tasks ${model.mineCount}", "All Tasks ${model.allCount}"),
            selectedIndex = if (model.scope == TaskScope.Mine) 0 else 1,
            onSelect = { onScope(if (it == 0) TaskScope.Mine else TaskScope.All) },
        )

        Column(Modifier.padding(top = HqSpacing.md), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            HqCalloutCard(
                title = if (model.away) "You're away" else "I'm away",
                support = if (model.away) "Tasks skip you until you return. Tap to return." else "Pause your turns while you're out of town.",
                icon = HqIcons.Clock,
                tone = HqTileTone.Neutral,
                onClick = onOpenAway,
            )
            if (model.pendingSwapsForMe > 0) {
                HqCalloutCard(
                    title = "Swap requests",
                    support = "${model.pendingSwapsForMe} waiting for your answer",
                    icon = HqIcons.Users,
                    tone = HqTileTone.Teal,
                    onClick = onReviewSwaps,
                )
            }
        }

        if (model.sections.isEmpty()) {
            EmptyTasks(model, onCreateTask)
        }

        model.sections.forEach { section ->
            HqListHeading(section.title, right = "${section.items.size}")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                section.items.forEach { task ->
                    HqLargeTaskCard(
                        title = task.name,
                        due = task.dueText,
                        assigneeName = if (task.assigneeText == "You") "You" else task.assigneeText,
                        assigneeLabel = task.assigneeText,
                        assigneeIsMe = task.assigneeText == "You",
                        badge = if (task.overdue && !task.completed) "Overdue" else null,
                        meta = if (task.overdue && !task.completed) null else when (task.kind) {
                            HomeTaskKind.Group -> "Group task"
                            HomeTaskKind.OneOff -> "One-time"
                            HomeTaskKind.Rotating -> "Rotation"
                        },
                        metaIcon = if (task.kind == HomeTaskKind.Rotating) HqIcons.Clock else null,
                        overdue = task.overdue && !task.completed,
                        quiet = task.completed || section.quiet,
                        done = task.completed,
                        canComplete = task.canComplete,
                        completing = completingTaskId == task.id,
                        onOpen = { onOpenTask(task.id) },
                        onComplete = { onCompleteTask(task.id) },
                    )
                }
            }
        }
    }
}

/** Shown only after a successful load found nothing, with the one action that role can take. */
@Composable
private fun EmptyTasks(model: TasksUiModel, onCreateTask: () -> Unit) {
    val c = LocalHqColors.current
    Column(
        Modifier.fillMaxWidth().padding(vertical = HqSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HqSpacing.related),
    ) {
        HqIllustration(HqArt.Checklist, Modifier.width(88.dp))
        when {
            model.overdueOnly -> Text("Nothing is overdue.", style = HqType.bodyLarge, color = c.textSecondary)
            model.scope == TaskScope.Mine -> Text("No tasks assigned to you.", style = HqType.bodyLarge, color = c.textSecondary)
            model.isAdmin -> {
                Text("No tasks yet", style = HqType.titleSmall2, color = c.textPrimary)
                Text("Create a simple rotation so everyone knows whose turn it is.", style = HqType.bodyMedium, color = c.textSecondary)
                HqButton(text = "Create task", onClick = onCreateTask, fullWidth = false)
            }
            else -> Text("No tasks yet.", style = HqType.bodyLarge, color = c.textSecondary)
        }
    }
}
