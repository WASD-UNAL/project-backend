package app.gymly.dto.membership

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class PlanCreate(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    var name: String,

    @field:Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    var description: String?,

    @field:NotNull(message = "La duración en días es obligatoria")
    @field:Min(value = 1, message = "La duración mínima debe ser de 1 día")
    var durationDays: Int,

    @field:NotNull(message = "El precio es obligatorio")
    @field:DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    var price: BigDecimal,

    @field:NotNull(message = "El estado activo es obligatorio")
    var active: Boolean
)