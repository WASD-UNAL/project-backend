package app.gymly.model

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class Roles(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false)
    var name: String = ""
)