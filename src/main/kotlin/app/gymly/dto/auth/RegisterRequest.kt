package app.gymly.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val lastname: String,
    @field:NotBlank
    @field:Email
    val email: String,
    @field:NotBlank
    val document: String,
    @field:NotBlank
    @field:Size(min = 8)
    val password: String,
)
