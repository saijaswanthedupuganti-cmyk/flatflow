package habitiq.app.screenshots

import habitiq.app.lib.formatInr
import habitiq.app.ui.HomeActivityItem
import habitiq.app.ui.HomeBalance
import habitiq.app.ui.HomeContent
import habitiq.app.ui.HomePendingItem
import habitiq.app.ui.HomeTaskItem
import habitiq.app.ui.HomeTaskKind
import habitiq.app.ui.HomeUiModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeScreenshotTest : ScreenshotHarness() {
    private val full = HomeUiModel(
        flatName = "Sunrise Heights 4B",
        roleLabel = "Admin",
        canInvite = true,
        greetingName = "Sai",
        summaryHeadline = "1 task overdue.",
        assignedCount = 3,
        memberCount = 4,
        tasks = listOf(
            HomeTaskItem("1", "Kitchen cleanup", "You", "Was due yesterday", overdue = true, canComplete = true),
            HomeTaskItem("2", "Take out the trash", "You", "Due today", overdue = false, canComplete = true),
            HomeTaskItem("3", "Pay the wifi bill together", "You", "Due Oct 4", overdue = false, canComplete = true, kind = HomeTaskKind.Group),
        ),
        pending = listOf(
            HomePendingItem(HomePendingItem.Kind.JoinRequests, "Join requests", "2 people waiting for approval"),
            HomePendingItem(HomePendingItem.Kind.SwapRequests, "Swap requests", "1 request waiting for your answer"),
        ),
        balance = HomeBalance(owe = 850.0, owed = 1240.5, oweCount = 1, owedCount = 2),
        balanceText = { formatInr(it) },
        activity = listOf(
            HomeActivityItem("Anita completed Kitchen cleanup", "2 hours ago"),
            HomeActivityItem("You added an expense: Groceries", "Yesterday"),
        ),
    )

    private fun render(name: String, model: HomeUiModel, dark: Boolean = false, font: Float = 1f, completing: String? = null) =
        shoot(name, dark = dark, fontScale = font) {
            HomeContent(model, completing, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
        }

    @Test fun light() = render("home-light", full)
    @Test fun dark() = render("home-dark", full, dark = true)
    @Test fun empty() = render("home-empty", full.copy(tasks = emptyList(), pending = emptyList(), activity = emptyList(), assignedCount = 0, summaryHeadline = "You're all caught up.", balance = HomeBalance(0.0, 0.0, 0, 0)))
}
