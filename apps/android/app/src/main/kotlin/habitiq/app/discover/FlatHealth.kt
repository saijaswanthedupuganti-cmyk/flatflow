package habitiq.app.discover

import habitiq.app.data.FlatActivity
import habitiq.app.data.FlatTask
import habitiq.app.data.Settlement
import habitiq.app.flats.Member

/**
 * Qualitative household signals for Discovery.
 * Never a leaderboard, never a 9.4/10, never a percentage in the UI.
 * Computed only from data the publishing admin can already read, then snapshotted
 * onto the public flat document so seekers do not need member-subcollection access.
 */
data class PublicMemberSummary(
    val nickname: String,
    val role: String
)

data class FlatHealthSnapshot(
    val headline: String,
    val taskSignal: String,
    val settlementSignal: String,
    val rotationSignal: String,
    val activitySignal: String,
    val stabilitySignal: String,
    val members: List<PublicMemberSummary> = emptyList(),
    val updatedAt: String = ""
) {
    fun rows(): List<Pair<String, String>> = listOf(
        "Tasks" to label(taskSignal),
        "Expenses" to label(settlementSignal),
        "Rotation" to label(rotationSignal),
        "Activity" to label(activitySignal),
        "Members" to label(stabilitySignal)
    )

    companion object {
        const val LIMITED = "limited_history"
        const val CONSISTENT = "consistently_completed"
        const val SOME_OVERDUE = "some_overdue"
        const val SETTLED = "regularly_settled"
        const val EXPENSES_RECORDED = "expenses_recorded"
        const val ROTATION_ACTIVE = "rotation_active"
        const val ACTIVE = "active"
        const val QUIET = "quiet"
        const val SHARED = "shared_household"
        const val SMALL = "small_household"

        fun empty(): FlatHealthSnapshot = FlatHealthSnapshot(
            headline = "Limited history",
            taskSignal = LIMITED,
            settlementSignal = LIMITED,
            rotationSignal = LIMITED,
            activitySignal = LIMITED,
            stabilitySignal = LIMITED
        )

        fun label(code: String): String = when (code) {
            CONSISTENT -> "Consistently completed"
            SOME_OVERDUE -> "Some tasks overdue"
            SETTLED -> "Regularly settled"
            EXPENSES_RECORDED -> "Expenses are recorded"
            ROTATION_ACTIVE -> "Active"
            ACTIVE -> "Active"
            QUIET -> "Quiet lately"
            SHARED -> "Shared household"
            SMALL -> "Small household"
            else -> "Not enough history"
        }

        fun parse(raw: Any?): FlatHealthSnapshot? {
            val map = raw as? Map<*, *> ?: return null
            val membersRaw = map["members"] as? List<*> ?: emptyList<Any>()
            val members = membersRaw.mapNotNull { item ->
                val m = item as? Map<*, *> ?: return@mapNotNull null
                val nick = m["nickname"]?.toString()?.trim().orEmpty()
                if (nick.isBlank()) return@mapNotNull null
                PublicMemberSummary(nick, m["role"]?.toString() ?: "member")
            }
            return FlatHealthSnapshot(
                headline = map["headline"]?.toString().orEmpty().ifBlank { "Limited history" },
                taskSignal = map["taskSignal"]?.toString() ?: LIMITED,
                settlementSignal = map["settlementSignal"]?.toString() ?: LIMITED,
                rotationSignal = map["rotationSignal"]?.toString() ?: LIMITED,
                activitySignal = map["activitySignal"]?.toString() ?: LIMITED,
                stabilitySignal = map["stabilitySignal"]?.toString() ?: LIMITED,
                members = members,
                updatedAt = map["updatedAt"]?.toString().orEmpty()
            )
        }
    }

    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "headline" to headline,
        "taskSignal" to taskSignal,
        "settlementSignal" to settlementSignal,
        "rotationSignal" to rotationSignal,
        "activitySignal" to activitySignal,
        "stabilitySignal" to stabilitySignal,
        "members" to members.map { mapOf("nickname" to it.nickname, "role" to it.role) },
        "updatedAt" to updatedAt
    )
}

object FlatHealthComputer {
    fun compute(
        tasks: List<FlatTask>,
        settlements: List<Settlement>,
        expensesCount: Int,
        activity: List<FlatActivity>,
        members: List<Member>,
        nowIso: String
    ): FlatHealthSnapshot {
        val overdueCount = tasks.count { it.status == "overdue" }
        val completedCount = tasks.count { it.status == "completed" }
        val taskSignal = when {
            tasks.isEmpty() -> FlatHealthSnapshot.LIMITED
            overdueCount > 0 && completedCount == 0 -> FlatHealthSnapshot.SOME_OVERDUE
            completedCount > 0 -> FlatHealthSnapshot.CONSISTENT
            else -> FlatHealthSnapshot.LIMITED
        }
        val settlementSignal = when {
            settlements.isNotEmpty() -> FlatHealthSnapshot.SETTLED
            expensesCount > 0 -> FlatHealthSnapshot.EXPENSES_RECORDED
            else -> FlatHealthSnapshot.LIMITED
        }
        val rotationSignal = if (tasks.any { it.type == "rotating_duty" && it.queueOrder.size > 1 }) {
            FlatHealthSnapshot.ROTATION_ACTIVE
        } else {
            FlatHealthSnapshot.LIMITED
        }
        val activitySignal = if (activity.isNotEmpty() || tasks.any { it.lastCompletedAt.isNotBlank() }) {
            FlatHealthSnapshot.ACTIVE
        } else {
            FlatHealthSnapshot.QUIET
        }
        val stabilitySignal = if (members.size >= 2) FlatHealthSnapshot.SHARED else FlatHealthSnapshot.SMALL

        val known = listOf(taskSignal, settlementSignal, rotationSignal).count { it != FlatHealthSnapshot.LIMITED }
        val headline = when {
            known >= 2 && taskSignal == FlatHealthSnapshot.CONSISTENT -> "Well maintained"
            known >= 1 && activitySignal == FlatHealthSnapshot.ACTIVE -> "Actively maintained"
            members.isNotEmpty() -> "New household"
            else -> "Limited history"
        }

        return FlatHealthSnapshot(
            headline = headline,
            taskSignal = taskSignal,
            settlementSignal = settlementSignal,
            rotationSignal = rotationSignal,
            activitySignal = activitySignal,
            stabilitySignal = stabilitySignal,
            members = members.map { PublicMemberSummary(it.nickname.ifBlank { "Member" }, it.role) },
            updatedAt = nowIso
        )
    }
}
