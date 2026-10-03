package habitiq.app.screenshots

import habitiq.app.ui.figma.CreateTaskTypeScreen
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskTypeScreenshotTest : ScreenshotHarness() {
    @Test fun light() = shoot("task-type-light") { CreateTaskTypeScreen(onBack = {}, onSelect = {}) }
    @Test fun dark() = shoot("task-type-dark", dark = true) { CreateTaskTypeScreen(onBack = {}, onSelect = {}, exampleNames = listOf("Sai Jaswanth", "Ravi")) }
}
