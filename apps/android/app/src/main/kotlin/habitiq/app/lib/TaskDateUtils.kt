package habitiq.app.lib

import habitiq.app.data.FlatTask
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** Parse Firestore dueDate values (ISO instant or yyyy-MM-dd). */
fun parseTaskInstant(value: String): Instant? = runCatching {
    when {
        value.isBlank() -> null
        value.length == 10 -> LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant()
        else -> Instant.parse(value)
    }
}.getOrNull()

fun parseTaskLocalDate(value: String): LocalDate? = runCatching {
    when {
        value.isBlank() -> null
        value.length == 10 -> LocalDate.parse(value)
        else -> parseTaskInstant(value)?.atZone(ZoneId.systemDefault())?.toLocalDate()
    }
}.getOrNull()

fun isDueToday(dueDate: String): Boolean =
    parseTaskLocalDate(dueDate) == LocalDate.now()

fun isTaskOverdue(dueDate: String, status: String): Boolean {
    if (status == "completed" || status == "paused") return false
    val due = parseTaskLocalDate(dueDate) ?: return false
    return due.isBefore(LocalDate.now())
}

fun effectiveTaskStatus(task: FlatTask): String =
    if (task.status == "pending" && isTaskOverdue(task.dueDate, task.status)) "overdue" else task.status

/** Whole days since [dueDate], for "Nd overdue" attention copy. 0 if not parseable/not overdue. */
fun daysOverdue(dueDate: String): Long {
    val due = parseTaskLocalDate(dueDate) ?: return 0
    return ChronoUnit.DAYS.between(due, LocalDate.now()).coerceAtLeast(0)
}

fun defaultDueDateIso(daysFromNow: Long = 7): String =
    Instant.now().plus(daysFromNow, ChronoUnit.DAYS).toString()

fun formatDueLabel(dueDate: String): String = when (parseTaskLocalDate(dueDate)) {
    LocalDate.now() -> "Due Today"
    LocalDate.now().plusDays(1) -> "Due Tomorrow"
    null -> "Due soon"
    else -> "Due ${parseTaskLocalDate(dueDate)?.format(DateTimeFormatter.ofPattern("MMM d"))}"
}

fun formatExpectedDue(dueDate: String): String = runCatching {
    val date = parseTaskLocalDate(dueDate) ?: return@runCatching "soon"
    date.format(DateTimeFormatter.ofPattern("EEE dd MMM"))
}.getOrDefault("soon")
