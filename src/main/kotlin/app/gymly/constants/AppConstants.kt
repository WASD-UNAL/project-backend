package app.gymly.constants

/**
 * Constantes de la aplicación siguiendo Clean Architecture.
 * Centraliza todas las cadenas de caracteres que se repiten en el código.
 */
object AppConstants {

    // Roles - Minúsculas (para base de datos)
    const val ROLE_CLIENT = "client"
    const val ROLE_ADMIN = "admin"

    // Roles - Mayúsculas (para seguridad de Spring)
    const val ROLE_CLIENT_UPPER = "CLIENT"
    const val ROLE_ADMIN_UPPER = "ADMIN"
}

