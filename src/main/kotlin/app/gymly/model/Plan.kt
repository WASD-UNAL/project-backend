package app.gymly.model

import jakarta.persistence.*

@Entity
@Table(name = "plan")
class Plan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "duration_days", nullable = false)
    var durationDays: Int,

    @Column(nullable = false)
    var price: Int,

    @Column(nullable = false)
    var active: Boolean = true
)