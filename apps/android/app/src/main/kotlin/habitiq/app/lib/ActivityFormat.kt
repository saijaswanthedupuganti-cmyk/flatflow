package habitiq.app.lib

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Plain-language label for an activity-log action key. Unknown keys are humanised, never shown raw. */
fun activityActionLabel(action: String): String = when (action) {
    "completed_task" -> "Task completed"
    "task_created" -> "Task created"
    "task_edited" -> "Task edited"
    "task_deleted" -> "Task deleted"
    "transferred_task" -> "Task handed over"
    "system_override" -> "Assignment changed by an admin"
    "expense_added" -> "Expense added"
    "expense_edited" -> "Expense edited"
    "expense_deleted" -> "Expense deleted"
    else -> action.replace('_', ' ').trim().replaceFirstChar { it.uppercase() }.ifBlank { "Update" }
}

private val activityTimeFormat = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.getDefault())

/** Unambiguous local date and time, e.g. "1 Oct 2026, 10:42 pm". Falls back to the date part if unparseable. */
fun formatActivityTime(iso: String, zone: ZoneId = ZoneId.systemDefault()): String =
    runCatching { activityTimeFormat.format(Instant.parse(iso).atZone(zone)) }
        .getOrElse { iso.take(10) }

/**
 * Relative time for a stored ISO instant ("Just now", "5 min ago", "3 hours ago", "2 days ago").
 * Uses instants end to end so the device's UTC offset cannot skew the result.
 */
fun formatTimeAgo(iso: String, now: Instant = Instant.now()): String = runCatching {
    val elapsed = java.time.Duration.between(Instant.parse(iso), now)
    val minutes = elapsed.toMinutes()
    val hours = elapsed.toHours()
    val days = elapsed.toDays()
    fun plural(n: Long, unit: String) = "$n $unit${if (n == 1L) "" else "s"} ago"
    when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> plural(hours, "hour")
        else -> plural(days, "day")
    }
}.getOrDefault("Recently")

private val displayDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

/** Unambiguous date such as "1 Oct 2026" from a stored ISO date or instant; the raw text if it can't be read. */
fun formatDisplayDate(iso: String, zone: ZoneId = ZoneId.systemDefault()): String =
    runCatching {
        val date = if (iso.length <= 10) java.time.LocalDate.parse(iso) else Instant.parse(iso).atZone(zone).toLocalDate()
        displayDateFormat.format(date)
    }.getOrElse { iso }
