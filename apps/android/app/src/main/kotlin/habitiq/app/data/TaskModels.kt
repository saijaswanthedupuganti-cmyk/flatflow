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
    val lastCompletedAt: String = "",
    val notes: String = ""
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
        "lastCompletedAt" to lastCompletedAt,
        "notes" to notes
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
    val createdAt: String = "",
    val isOOSRequest: Boolean = false
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
    val about: String,
    val flatType: String = "",
    val adminUid: String = "",
    val memberCount: Int = 0,
    val lifestyle: List<String> = emptyList(),
    val customTags: List<String> = emptyList(),
    val existingMembersGender: String? = null,
    /** Null means a legacy listing whose room type was never recorded. */
    val roomType: String? = null,
    val securityDeposit: Double? = null,
    val availableFrom: String = "",
    val noticePeriod: String = "",
    val furnishing: String = "",
    val amenities: List<String> = emptyList(),
    val preferredOccupation: String = "",
    val preferredBudgetMin: Double? = null,
    val preferredBudgetMax: Double? = null,
    val moveInTiming: String = "",
    val approximateLocationOnly: Boolean = true,
    val photoUrls: List<String> = emptyList(),
    val updatedAt: String = "",
    val postStatus: String? = null,
    val health: habitiq.app.discover.FlatHealthSnapshot? = null,
    val approxLocation: habitiq.app.discover.ApproxNeighborhoodLocation? = null
) {
    /** Merged tags from vacancy.lifestyle + vacancy.customTags (web schema). */
    fun displayTags(): List<String> = (lifestyle + customTags).map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}

data class UserProfileData(
    val uid: String,
    val email: String,
    val displayName: String,
    val activeFlatId: String?,
    val flatIds: List<String> = emptyList()
)

data class JoinRequest(
    val id: String,
    val uid: String,
    val nickname: String,
    val email: String,
    val status: String = "pending",
    val createdAt: String = ""
)

data class VacancyData(
    val active: Boolean = false,
    val city: String = "",
    val area: String = "",
    val rentPerHead: Double? = null,
    val currency: String = "INR",
    val bedsAvailable: Int = 1,
    val preferredGender: String = "any",
    val about: String = "",
    val lifestyle: List<String> = emptyList(),
    val customTags: List<String> = emptyList(),
    val roomType: String = "private",
    val securityDeposit: Double? = null,
    val availableFrom: String = "",
    val noticePeriod: String = "any",
    val furnishing: String = "furnished",
    val amenities: List<String> = emptyList(),
    val preferredOccupation: String = "any",
    val preferredBudgetMin: Double? = null,
    val preferredBudgetMax: Double? = null,
    val moveInTiming: String = "within_1_month",
    val approximateLocationOnly: Boolean = true,
    val photoUrls: List<String> = emptyList(),
    val existingMembersGender: String? = null,
    val updatedAt: String = "",
    val postStatus: String? = null
)
