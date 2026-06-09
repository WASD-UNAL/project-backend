package app.gymly.dto.membership

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

data class DiscountCreate(
    @field:NotNull(message = "El porcentaje es obligatorio")
    @field:Min(value = 1, message = "El descuento mínimo es 1%")
    @field:Max(value = 100, message = "El descuento máximo es 100%")
    var percentage: BigDecimal,

    @field:NotNull(message = "La fecha de inicio es obligatoria")
    var initDate: LocalDate,

    @field:NotNull(message = "La fecha de fin es obligatoria")
    var endDate: LocalDate,

    @field:NotNull(message = "El estado activo es obligatorio")
    var active: Boolean
)