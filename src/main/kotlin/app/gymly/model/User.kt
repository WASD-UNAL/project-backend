package app.gymly.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class User(
    @Column(name = "role_id", nullable = false)
    var roleId: Int,
    @Column(nullable = false)
    var name: String,
    @Column(name = "last_name", nullable = false)
    var lastname: String,
    @Column(unique = true)
    var email: String,
    @Column(name = "password_hash",nullable = false)
    var passwordHash: String,
    @Column(unique = true ,nullable = false)
    var document: Int,
    @Column(name = "created_at", insertable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,
    @Column(nullable = false)
    var active: Boolean = true
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}