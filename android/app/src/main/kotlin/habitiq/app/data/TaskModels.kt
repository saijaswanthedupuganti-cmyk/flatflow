package habitiq.app.data

data class FlatTask(
    val taskId: String,
    val name: String,
    val type: String = "rotating_duty",
    val currentAssignedUserId: String,
    val status: String,
    val dueDate: String,
    val priority: String = "medium",
    val frequency: String = "weekly",
    val queueOrder: List<String> = emptyList(),
    val lastCompletedAt: String = ""
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "taskId" to taskId,
        "name" to name,
        "type" to type,
        "currentAssignedUserId" to currentAssignedUserId,
        "status" to status,
        "dueDate" to dueDate,
        "priority" to priority,
        "frequency" to frequency,
        "queueOrder" to queueOrder,
        "lastCompletedAt" to lastCompletedAt
    )
}

data class FlatActivity(
    val id: String,
    val timestamp: String,
    val userId: String,
    val action: String,
    val details: String
)

data class FlatExpense(
    val id: String,
    val description: String,
    val amount: Double,
    val currency: String = "INR",
    val paidBy: String,
    val splitAmong: List<String>,
    val splitType: String = "equal",
    val splits: Map<String, Double> = emptyMap(),
    val category: String = "other",
    val date: String,
    val createdBy: String,
    val createdAt: String = ""
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "description" to description,
        "amount" to amount,
        "currency" to currency,
        "paidBy" to paidBy,
        "splitAmong" to splitAmong,
        "splitType" to splitType,
        "splits" to splits,
        "category" to category,
        "date" to date,
        "createdBy" to createdBy,
        "createdAt" to createdAt
    )
}

data class FlatSwapRequest(
    val id: String,
    val taskId: String,
    val fromUserId: String,
    val toUserId: String,
    val status: String,
    val read: Boolean = false,
    val createdAt: String = ""
)

data class VacancyListing(
    val flatId: String,
    val flatName: String,
    val active: Boolean,
    val city: String,
    val area: String,
    val rentPerHead: Double?,
    val currency: String,
    val bedsAvailable: Int,
    val preferredGender: String,
    val about: String
)

data class UserProfileData(
    val uid: String,
    val email: String,
    val displayName: String,
    val activeFlatId: String?,
    val flatIds: List<String> = emptyList()
)
