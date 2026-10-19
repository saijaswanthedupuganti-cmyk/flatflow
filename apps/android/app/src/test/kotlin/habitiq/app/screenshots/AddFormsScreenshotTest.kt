package habitiq.app.screenshots

import habitiq.app.flats.Member
import habitiq.app.ui.AddExpenseForm
import habitiq.app.ui.figma.AddTaskForm
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The single Add task form (replaces the old type picker + two forms) and the reordered Add expense form. */
@RunWith(RobolectricTestRunner::class)
class AddFormsScreenshotTest : ScreenshotHarness() {
    private val members = listOf(
        Member("u1", "Sai Jaswanth", "admin"),
        Member("u2", "Ravi Kumar", "member"),
        Member("u3", "Priya", "member"),
    )

    private fun task(name: String, dark: Boolean = false, font: Float = 1f) = shoot(name, dark = dark, fontScale = font) {
        AddTaskForm(members, currentUid = "u1", onBack = {}, onSubmit = { _, _ -> }, onAdded = {}, autoFocus = false)
    }

    private fun expense(name: String, dark: Boolean = false, font: Float = 1f) = shoot(name, dark = dark, fontScale = font) {
        AddExpenseForm(members, currentUid = "u1", onBack = {}, onSubmit = { _, _ -> }, onSaved = {}, autoFocus = false)
    }

    @Test fun addTaskLight() = task("add-task-light")
    @Test fun addTaskDark() = task("add-task-dark", dark = true)
    @Test fun addTaskLargeFont() = task("add-task-font200", font = 2f)
    @Test fun addExpenseLight() = expense("add-expense-light")
    @Test fun addExpenseDark() = expense("add-expense-dark", dark = true)
}
