package app.gymly.dto.stats

data class VisualAlertResponse(
    val document: String,
    val userName: String?,
    val statusColor: String, // "GREEN", "YELLOW", "RED"
    val daysRemaining: Long,
    val message: String,
    val userId: Int? = null,
)
