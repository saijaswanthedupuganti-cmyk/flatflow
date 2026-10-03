package habitiq.app.agent

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatePhrasesTest {
    private val saturday = LocalDate.of(2026, 10, 3)
    private val wednesday = LocalDate.of(2026, 10, 7)
    private fun d(day: Int) = LocalDate.of(2026, 10, day)

    @Test fun `single days`() {
        assertEquals(d(3)..d(3), parseDateRange("my tasks today", saturday))
        assertEquals(d(4)..d(4), parseDateRange("my tasks tomorrow", saturday))
        assertEquals(d(5)..d(5), parseDateRange("day after tomorrow", saturday))
    }

    @Test fun `weeks and weekends`() {
        assertEquals(d(3)..d(4), parseDateRange("this week", saturday))
        assertEquals(d(5)..d(11), parseDateRange("next week", saturday))
        assertEquals(d(3)..d(4), parseDateRange("this weekend", saturday))
        assertEquals(d(10)..d(11), parseDateRange("weekend", wednesday))
        assertEquals(d(7)..d(11), parseDateRange("this week", wednesday))
    }

    @Test fun `no phrase gives null`() {
        assertNull(parseDateRange("my tasks", saturday))
        assertNull(parseDateRange("todays", saturday))
    }
}
