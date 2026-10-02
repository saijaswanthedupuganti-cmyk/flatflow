package habitiq.app.lib

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskDueLabelTest {
    private fun iso(daysFromToday: Long) = LocalDate.now().plusDays(daysFromToday).toString()

    @Test fun `due labels use sentence case`() {
        assertEquals("Due today", formatDueLabel(iso(0)))
        assertEquals("Due tomorrow", formatDueLabel(iso(1)))
    }

    @Test fun `overdue wording is factual and names the date`() {
        assertEquals("Was due yesterday", formatWasDueLabel(iso(-1)))
        val older = LocalDate.now().minusDays(5)
        assertEquals("Was due ${older.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())} ${older.dayOfMonth}", formatWasDueLabel(older.toString()))
    }

    @Test fun `unparseable dates fall back without blame`() {
        assertEquals("Past due", formatWasDueLabel("not-a-date"))
    }
}
