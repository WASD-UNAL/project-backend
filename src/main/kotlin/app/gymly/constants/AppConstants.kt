package app.gymly.constants

import java.time.ZoneId

object AppConstants {
    const val ROLE_CLIENT = "client"
    const val ROLE_ADMIN = "admin"

    const val ROLE_CLIENT_UPPER = "CLIENT"
    const val ROLE_ADMIN_UPPER = "ADMIN"

    val APP_ZONE_ID: ZoneId = ZoneId.of("America/Bogota")
}
