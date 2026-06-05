package app.gymly.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "attendances")
class Attendance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Int,

    @Column(name = "date", insertable = false, updatable = false)
    val date: OffsetDateTime? = null
)

