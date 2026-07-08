package app.gymly.dto.membership

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate

data class DiscountRequest(
    @field:NotBlank(message = "El nombre promocional es obligatorio")
    @field:Size(max = 60, message = "El nombre promocional no puede superar 60 caracteres")
    var name: String,
    @field:Size(max = 200, message = "La descripción no puede superar 200 caracteres")
    var description: String? = null,
    @field:NotNull(message = "El porcentaje es obligatorio")
    @field:Min(value = 1, message = "El descuento mínimo es 1%")
    @field:Max(value = 100, message = "El descuento máximo es 100%")
    var percentage: BigDecimal,
    @field:NotNull(message = "La fecha de inicio es obligatoria")
    var initDate: LocalDate,
    @field:NotNull(message = "La fecha de fin es obligatoria")
    var endDate: LocalDate,
    @field:NotNull(message = "El estado activo es obligatorio")
    var active: Boolean,
    var planIds: List<Int> = emptyList(),
)
