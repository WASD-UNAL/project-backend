package app.gymly.dto.payment

import jakarta.validation.constraints.NotNull

data class ConfirmCheckoutRequest(
    @field:NotNull(message = "El ID del pago de Mercado Pago es requerido")
    var mpPaymentId: Long?,
)
