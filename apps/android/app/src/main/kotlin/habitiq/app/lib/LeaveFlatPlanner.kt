package habitiq.app.lib

/** The bits of a member the leave plan needs. [joinedAt] is an ISO instant; blank sorts last. */
data class LeaveMember(val uid: String, val role: String, val joinedAt: String)

/** The bits of a task the leave plan needs. */
data class LeaveTask(val taskId: String, val assignedUid: String, val queueOrder: List<String>)

data class TaskReassignment(val taskId: String, val newAssignee: String, val newQueue: List<String>)

/**
 * What has to change in the flat for [leaverUid] to leave without leaving it in a broken state.
 *
 * - [closeFlat]: the leaver is the last member, so nobody is left to run it.
 * - [promoteUid]: the leaver is the only admin and others remain, so the longest-standing member
 *   takes over. Null when no handover is needed.
 * - [taskChanges]: tasks the leaver holds pass to the next person in that task's own rotation, and
 *   the leaver is removed from every queue they appear in.
 */
data class LeavePlan(
    val closeFlat: Boolean,
    val promoteUid: String?,
    val taskChanges: List<TaskReassignment>,
)

fun planLeave(leaverUid: String, members: List<LeaveMember>, tasks: List<LeaveTask>): LeavePlan {
    val others = members.filter { it.uid != leaverUid }
    if (others.isEmpty()) return LeavePlan(closeFlat = true, promoteUid = null, taskChanges = emptyList())

    val leaverIsAdmin = members.any { it.uid == leaverUid && it.role == "admin" }
    val promoteUid = if (leaverIsAdmin && others.none { it.role == "admin" }) {
        others.sortedWith(compareBy({ it.joinedAt.isBlank() }, { it.joinedAt }, { it.uid })).first().uid
    } else null

    val remaining = others.map { it.uid }.toSet()
    val changes = tasks.mapNotNull { task ->
        val inQueue = leaverUid in task.queueOrder
        if (task.assignedUid != leaverUid && !inQueue) return@mapNotNull null
        val newQueue = task.queueOrder.filter { it != leaverUid }
        val newAssignee = if (task.assignedUid != leaverUid) {
            task.assignedUid
        } else {
            // Next person after the leaver in this task's rotation who is still in the flat, wrapping round.
            val start = task.queueOrder.indexOf(leaverUid).coerceAtLeast(0)
            val ordered = task.queueOrder.drop(start + 1) + task.queueOrder.take(start)
            ordered.firstOrNull { it in remaining } ?: others.first().uid
        }
        TaskReassignment(task.taskId, newAssignee, newQueue)
    }
    return LeavePlan(closeFlat = false, promoteUid = promoteUid, taskChanges = changes)
}

/** Plain-language consequences shown before someone leaves, matching exactly what [planLeave] will do. */
fun leaveFlatConsequences(members: List<LeaveMember>, leaverUid: String, hasUnsettledBalances: Boolean): String {
    val plan = planLeave(leaverUid, members, emptyList())
    val parts = mutableListOf<String>()
    if (plan.closeFlat) {
        parts += "You're the only member, so this flat will be closed and its tasks and expenses will no longer be available."
        return parts.joinToString(" ")
    }
    parts += "Your tasks pass to the next person in each rotation."
    if (plan.promoteUid != null) parts += "You're the only admin, so the longest-standing member becomes admin."
    parts += "You'll lose access to this flat's tasks and expenses. To come back, you'll need its invite code."
    if (hasUnsettledBalances) parts += "You still have unsettled balances with flatmates, so it's best to settle first."
    return parts.joinToString(" ")
}
