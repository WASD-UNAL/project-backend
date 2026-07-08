package app.gymly.dto.membership

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate

data class UpdateDiscountRequest(
    @field:Size(max = 60, message = "El nombre promocional no puede superar 60 caracteres")
    var name: String? = null,
    @field:Size(max = 200, message = "La descripción no puede superar 200 caracteres")
    var description: String? = null,
    @field:Min(value = 1, message = "El descuento mínimo es 1%")
    @field:Max(value = 100, message = "El descuento máximo es 100%")
    var percentage: BigDecimal? = null,
    var initDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var active: Boolean? = null,
    var planIds: List<Int>? = null,
)
