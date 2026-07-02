package app.gymly.dto.membership

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Estado de la membresía del usuario autenticado para la sección de Pagos.
 * `statusColor`, `daysRemaining` y `message` los calcula el backend
 * (VisualAlertService); los datos del plan sólo se incluyen si hay una
 * membresía activa y vigente.
 */
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
