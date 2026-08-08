package habitiq.app.data

data class FlatTask(
    val taskId: String,
    val name: String,
    val currentAssignedUserId: String,
    val status: String,
    val dueDate: String,
    val priority: String = "medium"
)

data class FlatActivity(
    val id: String,
    val timestamp: String,
    val userId: String,
    val action: String,
    val details: String
)
