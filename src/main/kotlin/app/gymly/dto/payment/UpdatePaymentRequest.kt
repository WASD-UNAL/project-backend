package app.gymly.dto.payment

import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class UpdatePaymentRequest(
    @field:NotNull(message = "El monto es requerido")
    @field:DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    var amount: BigDecimal?,

    @field:NotNull(message = "El método de pago es requerido")
    var method: PaymentMethod?,

    var reference: String? = null,

    @field:NotNull(message = "El estado del pago es requerido")
    var status: PaymentStatus?
)