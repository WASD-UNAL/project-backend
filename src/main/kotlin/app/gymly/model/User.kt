package app.gymly.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(name = "role_id", nullable = false)
    var roleId: Int,
    @Column(nullable = false)
    var name: String,
    @Column(name = "last_name", nullable = false)
    var lastname: String,
    @Column(unique = true)
    var phone: String? = null,
    @Column(unique = true)
    var email: String,
    @Column(name = "password_hash",nullable = false)
    var passwordHash: String,
    @Column(unique = true ,nullable = false)
    var document: String,
    @Column(name = "created_at", insertable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,
    @Column(nullable = false)
    var active: Boolean = true
)