package app.gymly.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "attendances")
class Attendance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Int,

    @Column(name = "date", insertable = false, updatable = false)
    var date: OffsetDateTime? = null
)

