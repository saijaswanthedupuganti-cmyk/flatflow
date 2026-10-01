package habitiq.app.lib

import habitiq.app.data.FlatTask
import habitiq.app.flats.Member
import java.time.Instant
import java.time.temporal.ChronoUnit

object RotationEngine {
    fun getNextAssignee(task: FlatTask, members: List<Member>): String? {
        val queue = task.queueOrder
        if (queue.isEmpty()) return task.currentAssignedUserId

        val currentIndex = queue.indexOf(task.currentAssignedUserId)
        val searchStart = if (currentIndex == -1) 0 else currentIndex

        for (i in 1..queue.size) {
            val nextIndex = (searchStart + i) % queue.size
            val candidateId = queue[nextIndex]
            val member = members.find { it.uid == candidateId }
            if (member != null && (member.status == "available" || member.status == "busy")) {
                return candidateId
            }
        }
        return null
    }

    fun completeTask(task: FlatTask, members: List<Member>, completionInstant: Instant = Instant.now()): FlatTask {
        val nextAssignee = getNextAssignee(task, members)
        val completedAt = completionInstant.toString()

        if (task.frequency == "one_time") {
            return task.copy(status = "completed", lastCompletedAt = completedAt)
        }

        val nextDue = when (task.frequency) {
            "daily" -> completionInstant.plus(1, ChronoUnit.DAYS)
            "weekly" -> completionInstant.plus(7, ChronoUnit.DAYS)
            "fortnightly" -> completionInstant.plus(14, ChronoUnit.DAYS)
            "monthly" -> completionInstant.plus(30, ChronoUnit.DAYS)
            else -> completionInstant.plus(7, ChronoUnit.DAYS)
        }

        if (nextAssignee == null) {
            return task.copy(status = "paused", lastCompletedAt = completedAt)
        }

        return task.copy(
            status = "pending",
            currentAssignedUserId = nextAssignee,
            lastCompletedAt = completedAt,
            dueDate = nextDue.toString()
        )
    }
}
