package app.gymly.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notification_queue")
class NotificationQueue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Int,

    @Column(nullable = false)
    val message: String,

    @Column(name = "alert_type", nullable = false)
    val alertType: String,

    @Column(nullable = false)
    var processed: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)