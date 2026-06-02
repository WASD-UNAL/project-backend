package app.gymly.dto

data class ColorAlertDTO(
    val document: String,
    val userName: String?,
    val statusColor: String, // "GREEN", "YELLOW", "RED"
    val daysRemaining: Long,
    val message: String
)