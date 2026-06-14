package app.gymly.dto.membership

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.math.BigDecimal
import java.time.LocalDate

data class UpdateDiscountRequest(
    @field:Min(value = 1, message = "El descuento mínimo es 1%")
    @field:Max(value = 100, message = "El descuento máximo es 100%")
    var percentage: BigDecimal? = null,
    var initDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var active: Boolean? = null,
)
