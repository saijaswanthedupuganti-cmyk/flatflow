package habitiq.app.screenshots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.ui.TaskListItem
import habitiq.app.ui.TaskScope
import androidx.compose.foundation.layout.Arrangement
import habitiq.app.ui.TaskSection
import habitiq.app.ui.TasksContent
import habitiq.app.ui.TasksUiModel
import habitiq.app.ui.components.HqTagFlow
import habitiq.app.ui.components.HqRootAppBar
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TasksScreenshotTest : ScreenshotHarness() {
    private val mine = TasksUiModel(
        scope = TaskScope.Mine, overdueOnly = false, mineCount = 3, allCount = 7, overdueCount = 1,
        away = false, pendingSwapsForMe = 1, isAdmin = false,
        sections = listOf(
            TaskSection("Overdue", listOf(TaskListItem("1", "Kitchen cleanup", "You", "Was due yesterday", true, false, true))),
            TaskSection("Today", listOf(TaskListItem("2", "Take out the trash", "You", "Due today", false, false, true))),
            TaskSection("Upcoming", listOf(TaskListItem("3", "Deep clean the bathroom and balcony before the landlord visit", "You", "Due Oct 6", false, false, true))),
        ),
    )
    private val all = mine.copy(
        scope = TaskScope.All, isAdmin = true, away = true, pendingSwapsForMe = 0,
        sections = listOf(
            TaskSection("Overdue", listOf(TaskListItem("1", "Kitchen cleanup", "You", "Was due yesterday", true, false, true))),
            TaskSection("Today", listOf(
                TaskListItem("2", "Take out the trash", "Anita", "Due today", false, false, false),
                TaskListItem("4", "Water the plants", "Rahul", "Due today", false, false, false),
            )),
            TaskSection("Completed", listOf(TaskListItem("5", "Sweep the hall", "Meera", "Done", false, true, false)), quiet = true),
        ),
    )

    private fun render(name: String, model: TasksUiModel, dark: Boolean = false, font: Float = 1f) = shoot(name, dark = dark, fontScale = font) {
        Column {
            HqRootAppBar("Tasks")
            TasksContent(model, null, {}, {}, {}, {}, {}, {}, {})
        }
    }

    @Test fun member() = render("tasks-member", mine)
    @Test fun admin() = render("tasks-admin-away", all)
    @Test fun memberDark() = render("tasks-member-dark", mine, dark = true)
    @Test fun large() = render("tasks-member-200", mine, font = 2f)

    @Test fun tags() = shoot("tags") {
        val c = LocalHqColors.current
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("zero (nothing)", style = HqType.labelSmall, color = c.textSecondary)
            HqTagFlow(emptyList())
            Text("one", style = HqType.labelSmall, color = c.textSecondary)
            HqTagFlow(listOf("Quiet evenings"))
            Text("three", style = HqType.labelSmall, color = c.textSecondary)
            HqTagFlow(listOf("Quiet evenings", "Vegetarian", "No smoking"))
            Text("twelve, max 2 rows", style = HqType.labelSmall, color = c.textSecondary)
            HqTagFlow(listOf("Quiet evenings", "Vegetarian", "No smoking", "Early bird", "Pet friendly", "Night owl", "Fitness", "Cooks often", "Plant lover", "Remote work", "Music", "Clean"), maxRows = 2)
            Text("one very long tag", style = HqType.labelSmall, color = c.textSecondary)
            HqTagFlow(listOf("Prefers a calm home with minimal weekday guests and early nights", "Veg"))
        }
    }
}
