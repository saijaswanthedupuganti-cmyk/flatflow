package habitiq.app.flats

data class FlatInfo(
    val id: String,
    val name: String,
    val adminUid: String,
    val memberCount: Int,
    val joinMode: String = "auto",
    val vacancy: habitiq.app.data.VacancyData? = null
)

/**
 * A flat listed in Discover shows its id (which is also its invite code) to every signed-in user,
 * so while the listing is live nobody joins it directly: they send a request the admin approves.
 * firestore.rules enforces the same thing; this keeps the app on the path the rules allow.
 */
fun effectiveJoinMode(joinMode: String?, vacancyActive: Boolean): String =
    if (vacancyActive) "approval" else joinMode ?: "auto"
