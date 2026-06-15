package app.gymly.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @Column(name = "user_id", nullable = false)
    var userId: Int,
    @Column(nullable = false, unique = true)
    var token: String,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,
    @Column(name = "created_at")
    var createdAt: Instant? = null,
)
