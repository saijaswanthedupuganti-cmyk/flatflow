package habitiq.app.screenshots

import androidx.compose.foundation.layout.Column
import habitiq.app.ui.HomeActivityItem
import habitiq.app.ui.ManageAttention
import habitiq.app.ui.ManageContent
import habitiq.app.ui.ManageUiModel
import habitiq.app.ui.TileLine
import habitiq.app.ui.components.HqRootAppBar
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ManageScreenshotTest : ScreenshotHarness() {
    private val model = ManageUiModel(
        flatContext = "Sunrise Heights 4B · 4 members",
        tasksLines = listOf(TileLine("3 assigned"), TileLine("1 overdue", urgent = true)),
        expensesLines = listOf(TileLine("You owe ₹850"), TileLine("You're owed ₹1,240.50")),
        attention = listOf(
            ManageAttention(ManageAttention.Kind.OverdueTask, "Overdue task", "Kitchen cleanup · was due yesterday", "1"),
            ManageAttention(ManageAttention.Kind.Balance, "Balance to settle", "You owe ₹850 to 1 person"),
        ),
        activity = listOf(HomeActivityItem("Anita completed Kitchen cleanup", "2 hours ago"), HomeActivityItem("You added an expense: Groceries", "Yesterday")),
    )

    private fun render(name: String, m: ManageUiModel, dark: Boolean = false, font: Float = 1f) = shoot(name, dark = dark, fontScale = font) {
        Column { ManageContent(m, {}, {}, {}, {}) }
    }

    @Test fun normal() = render("manage-light", model)
    @Test fun large() = render("manage-200", model, font = 2f)
    @Test fun quiet() = render("manage-caught-up", model.copy(attention = emptyList(), tasksLines = listOf(TileLine("Nothing assigned to you")), expensesLines = listOf(TileLine("All settled")), activity = emptyList()), dark = true)
}
