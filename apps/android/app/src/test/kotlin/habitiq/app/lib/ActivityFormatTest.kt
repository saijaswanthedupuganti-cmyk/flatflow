package habitiq.app.lib

import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ActivityFormatTest {
    @Test fun `known actions read as plain language`() {
        assertEquals("Task completed", activityActionLabel("completed_task"))
        assertEquals("Expense added", activityActionLabel("expense_added"))
    }

    @Test fun `unknown actions are humanised and never raw`() {
        val label = activityActionLabel("bill_generated")
        assertEquals("Bill generated", label)
        assertFalse(label.contains('_'))
        assertEquals("Update", activityActionLabel(""))
    }

    @Test fun `timestamps show an unambiguous local date`() {
        val text = formatActivityTime("2026-10-01T17:12:00Z", ZoneId.of("UTC"))
        assertEquals(true, text.startsWith("1 Oct 2026, 5:12"))
    }

    @Test fun `bad timestamps fall back to the date part`() {
        assertEquals("2026-10-01", formatActivityTime("2026-10-01 garbage"))
    }
}

class TimeAgoTest {
    private val now = java.time.Instant.parse("2026-10-01T12:00:00Z")

    @Test fun `recent and relative times read naturally`() {
        assertEquals("Just now", formatTimeAgo("2026-10-01T11:59:40Z", now))
        assertEquals("5 min ago", formatTimeAgo("2026-10-01T11:55:00Z", now))
        assertEquals("1 hour ago", formatTimeAgo("2026-10-01T10:45:00Z", now))
        assertEquals("3 hours ago", formatTimeAgo("2026-10-01T09:00:00Z", now))
        assertEquals("1 day ago", formatTimeAgo("2026-09-30T09:00:00Z", now))
        assertEquals("4 days ago", formatTimeAgo("2026-09-27T12:00:00Z", now))
    }

    @Test fun `bad input never crashes`() {
        assertEquals("Recently", formatTimeAgo("nonsense", now))
    }
}

class DisplayDateTest {
    @Test fun `dates read unambiguously`() {
        assertEquals("1 Oct 2026", formatDisplayDate("2026-10-01"))
        assertEquals("1 Oct 2026", formatDisplayDate("2026-10-01T23:30:00Z", java.time.ZoneId.of("UTC")))
    }

    @Test fun `unreadable dates are returned as given`() {
        assertEquals("soon", formatDisplayDate("soon"))
    }
}
