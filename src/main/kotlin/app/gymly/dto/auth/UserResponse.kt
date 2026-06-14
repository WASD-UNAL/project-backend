package app.gymly.dto.auth

data class UserResponse(
    val id: Int,
    val name: String,
    val lastname: String,
    val email: String,
    val document: String,
    val role: String,
    val active: Boolean,
)
