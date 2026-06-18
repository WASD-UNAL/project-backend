package app.gymly.dto.payment

import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class PaymentRequest(
    @field:NotNull(message = "El ID de la membresía es requerido")
    var membershipId: Int?,

    @field:NotNull(message = "El ID del usuario es requerido")
    var userId: Int?,

    var discountId: Int? = null,

    @field:NotNull(message = "El monto es requerido")
    @field:DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    var amount: BigDecimal?,

    var method: PaymentMethod = PaymentMethod.CASH,

    var reference: String? = null,

    @field:NotNull(message = "El estado del pago es requerido")
    var status: PaymentStatus?
)