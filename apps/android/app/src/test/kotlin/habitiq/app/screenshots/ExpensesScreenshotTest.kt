package habitiq.app.screenshots

import androidx.compose.foundation.layout.Column
import habitiq.app.ui.ContributionLine
import habitiq.app.ui.ExpenseListItem
import habitiq.app.ui.ExpensesContent
import habitiq.app.ui.ExpensesUiModel
import habitiq.app.ui.PersonBalanceItem
import habitiq.app.ui.components.HqRootAppBar
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExpensesScreenshotTest : ScreenshotHarness() {
    private val both = ExpensesUiModel(
        oweText = "₹850", oweSupport = "To 1 person", owedText = "₹1,240.50", owedSupport = "From 2 people",
        people = listOf(
            PersonBalanceItem("a", "Rahul", youOwe = true, amountText = "₹850", contributions = listOf(
                ContributionLine("Groceries", "1 Oct 2026", "You owe ₹600"), ContributionLine("Wifi", "28 Sep 2026", "You owe ₹250"))),
            PersonBalanceItem("b", "Meera", youOwe = false, amountText = "₹840.50", contributions = emptyList()),
            PersonBalanceItem("c", "Anita", youOwe = false, amountText = "₹400", contributions = emptyList()),
        ),
        expenses = listOf(
            ExpenseListItem("1", "Groceries", "1 Oct 2026 · paid by Sai", "₹1,800"),
            ExpenseListItem("2", "Wifi bill for the whole building this month", "28 Sep 2026 · paid by Rahul", "₹750"),
        ),
    )

    private fun render(name: String, m: ExpensesUiModel, dark: Boolean = false, font: Float = 1f) = shoot(name, dark = dark, fontScale = font) {
        Column { HqRootAppBar("Expenses"); ExpensesContent(m, {}, {}, {}) }
    }

    @Test fun both() = render("expenses-both", both)
    @Test fun settled() = render("expenses-settled", both.copy(oweText = null, owedText = null, oweSupport = null, owedSupport = null, people = emptyList()))
    @Test fun empty() = render("expenses-empty", both.copy(oweText = null, owedText = null, oweSupport = null, owedSupport = null, people = emptyList(), expenses = emptyList()), dark = true)
}
