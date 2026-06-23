package app.gymly.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "notification_queue")
class NotificationQueue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(name = "user_id", nullable = false)
    var userId: Int,
    @Column(nullable = false)
    var message: String,
    @Column(name = "alert_type", nullable = false)
    var alertType: String,
    @Column(nullable = false)
    var processed: Boolean = false,
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
)
