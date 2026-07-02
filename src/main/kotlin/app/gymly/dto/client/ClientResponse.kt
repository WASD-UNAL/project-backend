package app.gymly.dto.client

import java.math.BigDecimal
import java.time.OffsetDateTime

data class ClientResponse(
    val id: Int,
    val name: String,
    val lastname: String,
    val email: String,
    val document: String,
    val phone: String?,
    val weight: BigDecimal?,
    val height: BigDecimal?,
    val active: Boolean,
    val createdAt: OffsetDateTime?,
)
