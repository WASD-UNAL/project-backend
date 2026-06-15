package app.gymly.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "plans")
class Plan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(nullable = false)
    var name: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(name = "duration_days", nullable = false)
    var durationDays: Int,
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,
    @Column(nullable = false)
    var active: Boolean = true,
)
