package habitiq.app.agent

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** The date range a phrase in normalised [text] refers to, or null when it names none. M2 adds weekdays and "d MMM". */
fun parseDateRange(text: String, today: LocalDate): ClosedRange<LocalDate>? {
    fun has(pattern: String) = Regex("""\b(?:$pattern)\b""").containsMatchIn(text)
    val sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    return when {
        has("day after tomorrow") -> today.plusDays(2).let { it..it }
        has("tomorrow") -> today.plusDays(1).let { it..it }
        has("today|tonight") -> today..today
        has("next week") -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).let { it..it.plusDays(6) }
        has("this weekend|weekend") -> {
            val saturday = if (today.dayOfWeek == DayOfWeek.SUNDAY) today else today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            saturday..saturday.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        }
        has("this week") -> today..sunday
        else -> null
    }
}
