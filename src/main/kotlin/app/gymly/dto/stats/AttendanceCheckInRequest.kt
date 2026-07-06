package app.gymly.dto.stats

import jakarta.validation.constraints.NotBlank

data class AttendanceCheckInRequest(
    @field:NotBlank(message = "El número de documento es obligatorio.")
    val document: String,
)
