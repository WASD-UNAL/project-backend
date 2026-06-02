package app.gymly.dto.auth

import java.time.Instant

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresAt: Instant,
    val user: UserResponse
)
