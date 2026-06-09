package app.gymly.dto.membership

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class PlanUpdate(
    @field:Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    val name: String? = null,

    @field:Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    val description: String? = null,

    @field:Min(value = 1, message = "La duración mínima debe ser de 1 día")
    val durationDays: Int? = null,

    @field:DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    val price: BigDecimal? = null,

    val active: Boolean? = null
)