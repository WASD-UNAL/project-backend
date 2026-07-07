package app.gymly.dto.membership

import java.math.BigDecimal
import java.time.LocalDate

data class MyMembershipResponse(
    val hasActiveMembership: Boolean,
    val pendingApproval: Boolean,
    val membershipId: Int?,
    val planId: Int?,
    val planName: String?,
    val price: BigDecimal?,
    val initDate: LocalDate?,
    val endDate: LocalDate?,
    val statusColor: String,
    val daysRemaining: Long,
    val message: String,
)
