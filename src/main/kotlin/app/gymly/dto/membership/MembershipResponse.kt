package app.gymly.dto.membership

import app.gymly.model.MembershipStatus
import java.time.LocalDate

data class MembershipResponse(
    val id: Int,
    val userId: Int,
    val planId: Int,
    val initDate: LocalDate?,
    val endDate: LocalDate?,
    val status: MembershipStatus,
)
