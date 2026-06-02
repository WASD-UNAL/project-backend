package app.gymly.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "plans")
class Plan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "duration_days", nullable = false)
    var durationDays: Int,

    @Column(nullable = false)
    var price: BigDecimal,

    @Column(nullable = false)
    var active: Boolean = true
)