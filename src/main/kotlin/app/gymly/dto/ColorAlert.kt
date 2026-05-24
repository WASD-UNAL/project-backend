package app.gymly.dto

data class ColorAlert(
    val document: Int,
    val userName: String?,
    val statusColor: String, // "GREEN", "YELLOW", "RED"
    val daysRemaining: Long,
    val message: String
)