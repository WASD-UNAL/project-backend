package app.gymly.dto.membership

import java.math.BigDecimal
import java.time.LocalDate

data class MyMembershipResponse(
    val hasActiveMembership: Boolean,
    val planId: Int?,
    val planName: String?,
    val price: BigDecimal?,
    val initDate: LocalDate?,
    val endDate: LocalDate?,
    val statusColor: String,
    val daysRemaining: Long,
    val message: String,
)
