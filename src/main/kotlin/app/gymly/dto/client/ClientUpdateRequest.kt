package app.gymly.dto.client

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ClientUpdateRequest(
    @field:Size(max = 100)
    val name: String? = null,
    @field:Size(max = 100)
    val lastname: String? = null,
    @field:Email
    val email: String? = null,
    val document: String? = null,
    val phone: String? = null,
    val weight: BigDecimal? = null,
    val height: BigDecimal? = null,
    val active: Boolean? = null,
)
