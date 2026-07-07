package app.gymly.dto.auth

import java.math.BigDecimal

data class UserResponse(
    val id: Int,
    val name: String,
    val lastname: String,
    val email: String,
    val document: String,
    val role: String,
    val active: Boolean,
    val phone: String? = null,
    val weight: BigDecimal? = null,
    val height: BigDecimal? = null,
)
