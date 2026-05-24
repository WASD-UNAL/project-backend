package app.gymly.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "membership")
class Membership(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Int,

    @Column(name = "plan_id", nullable = false)
    var planId: Int,

    @Column(name = "init_date", nullable = false)
    var initDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,

    @Column(nullable = false)
    var status: String = "active",

    @Column(name = "created_at", insertable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
)