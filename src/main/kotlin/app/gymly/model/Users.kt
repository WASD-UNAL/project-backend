package app.gymly.model

import jakarta.persistence.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Entity
@Table(
    name = "users"
)
class User @OptIn(ExperimentalTime::class) constructor(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long,
    @Column(name = "role_id", nullable = false)
    var roleId: Int,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var lastname: String,
    var phone: String,
    @Column(name = "mail")
    var email: String,
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(nullable = false)
    var active: Boolean,
)
