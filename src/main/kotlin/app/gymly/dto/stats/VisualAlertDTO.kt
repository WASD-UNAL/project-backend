package app.gymly.dto.stats

data class VisualAlertDTO(
    val document: String,
    val userName: String?,
    val statusColor: String, // "GREEN", "YELLOW", "RED"
    val daysRemaining: Long,
    val message: String
)