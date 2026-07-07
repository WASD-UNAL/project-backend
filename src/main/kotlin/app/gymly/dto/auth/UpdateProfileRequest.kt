package app.gymly.dto.auth

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class UpdateProfileRequest(
    @field:Size(min = 1, max = 100)
    val name: String? = null,
    @field:Size(min = 1, max = 100)
    val lastname: String? = null,
    @field:Email
    val email: String? = null,
    @field:Size(max = 20)
    val phone: String? = null,
    @field:Positive
    @field:DecimalMax("999.99")
    val weight: BigDecimal? = null,
    @field:Positive
    @field:DecimalMax("999.99")
    val height: BigDecimal? = null,
)
