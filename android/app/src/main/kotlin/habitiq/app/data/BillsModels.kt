package habitiq.app.data

data class RecurringBill(
    val id: String,
    val name: String,
    val category: String = "bills",
    val amount: Double? = null,
    val currency: String = "INR",
    val billingDay: Int = 1,
    val rotationQueue: List<String> = emptyList(),
    val currentPayerIndex: Int = 0,
    val payerMode: String = "rotation",
    val fixedPayerUid: String? = null,
    val participants: List<String>? = null,
    val splitMethod: String = "equal",
    val isVariable: Boolean = false,
    val active: Boolean = true,
    val createdBy: String = "",
    val createdAt: String = "",
    val lastGeneratedMonth: String = "",
    val collectorId: String? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "category" to category,
        "amount" to amount,
        "currency" to currency,
        "billingDay" to billingDay,
        "rotationQueue" to rotationQueue,
        "currentPayerIndex" to currentPayerIndex,
        "payerMode" to payerMode,
        "fixedPayerUid" to fixedPayerUid,
        "participants" to participants,
        "splitMethod" to splitMethod,
        "isVariable" to isVariable,
        "active" to active,
        "createdBy" to createdBy,
        "createdAt" to createdAt,
        "lastGeneratedMonth" to lastGeneratedMonth,
        "collectorId" to collectorId
    )
}

data class BillInstance(
    val id: String,
    val templateId: String,
    val month: String,
    val name: String,
    val category: String = "bills",
    val currency: String = "INR",
    val amount: Double? = null,
    val paidBy: String,
    val participants: List<String> = emptyList(),
    val splits: Map<String, Double>? = null,
    val status: String = "pending",
    val dueDate: String,
    val paidAt: String? = null,
    val skippedReason: String? = null,
    val generatedAt: String = "",
    val generatedBy: String = "",
    val collectorId: String? = null,
    val collectedFrom: Map<String, Boolean>? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "templateId" to templateId,
        "month" to month,
        "name" to name,
        "category" to category,
        "currency" to currency,
        "amount" to amount,
        "paidBy" to paidBy,
        "participants" to participants,
        "splits" to splits,
        "status" to status,
        "dueDate" to dueDate,
        "paidAt" to paidAt,
        "skippedReason" to skippedReason,
        "generatedAt" to generatedAt,
        "generatedBy" to generatedBy,
        "collectorId" to collectorId,
        "collectedFrom" to collectedFrom
    )
}

data class MonthCycle(
    val id: String,
    val month: String,
    val status: String = "open",
    val closedAt: String? = null,
    val totalBillsINR: Double = 0.0,
    val totalExpensesINR: Double = 0.0,
    val totalSettledINR: Double = 0.0,
    val carryForwardOut: Map<String, Double>? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "month" to month,
        "status" to status,
        "closedAt" to closedAt,
        "totalBillsINR" to totalBillsINR,
        "totalExpensesINR" to totalExpensesINR,
        "totalSettledINR" to totalSettledINR,
        "carryForwardOut" to carryForwardOut?.let { mapOf("balances" to it) }
    )
}

data class Settlement(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Double,
    val currency: String = "INR",
    val note: String? = null,
    val date: String,
    val month: String? = null,
    val type: String = "immediate",
    val createdAt: String = ""
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "fromUserId" to fromUserId,
        "toUserId" to toUserId,
        "amount" to amount,
        "currency" to currency,
        "note" to note,
        "date" to date,
        "month" to month,
        "type" to type,
        "createdAt" to createdAt
    )
}

data class SeekerProfile(
    val id: String,
    val displayName: String,
    val photoUrl: String = "",
    val city: String,
    val lookingIn: String,
    val budget: Double,
    val bio: String = "",
    val lifestyleTags: String = "",
    val active: Boolean = true,
    val createdAt: String = ""
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "displayName" to displayName,
        "photoUrl" to photoUrl,
        "city" to city,
        "lookingIn" to lookingIn,
        "budget" to budget,
        "bio" to bio,
        "lifestyleTags" to lifestyleTags,
        "active" to active,
        "createdAt" to createdAt
    )
}

data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val flatId: String? = null,
    val content: String,
    val timestamp: Long,
    val isViewingRequest: Boolean = false,
    val viewingTime: Long? = null,
    val viewingStatus: String? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "senderId" to senderId,
        "receiverId" to receiverId,
        "flatId" to flatId,
        "content" to content,
        "timestamp" to timestamp,
        "isViewingRequest" to isViewingRequest,
        "viewingTime" to viewingTime,
        "viewingStatus" to viewingStatus
    )
}
