package habitiq.app.lib

import habitiq.app.data.FlatActivity
import habitiq.app.settings.ActivitySeen
import java.time.Instant

/** First-time installs treat the last day as new, so an old log does not open with a huge unread count. */
private const val FIRST_RUN_WINDOW_MILLIS = 24L * 60 * 60 * 1000

private fun millisOf(entry: FlatActivity): Long? = runCatching { Instant.parse(entry.timestamp).toEpochMilli() }.getOrNull()

/** True when the entry is someone else's update that this person has not seen yet. */
fun isUnread(entry: FlatActivity, uid: String, seen: ActivitySeen, nowMillis: Long = System.currentTimeMillis()): Boolean {
    if (entry.userId == uid || entry.id in seen.readIds) return false
    val floor = if (seen.seenUpToMillis > 0L) seen.seenUpToMillis else nowMillis - FIRST_RUN_WINDOW_MILLIS
    val at = millisOf(entry) ?: return false
    return at > floor
}

fun unreadCount(entries: List<FlatActivity>, uid: String, seen: ActivitySeen, nowMillis: Long = System.currentTimeMillis()): Int =
    entries.count { isUnread(it, uid, seen, nowMillis) }

/** Entries from the device's current day are "Today"; everything else is "Earlier". */
fun isToday(entry: FlatActivity, zone: java.time.ZoneId = java.time.ZoneId.systemDefault()): Boolean =
    runCatching { Instant.parse(entry.timestamp).atZone(zone).toLocalDate() == java.time.LocalDate.now(zone) }.getOrDefault(false)
