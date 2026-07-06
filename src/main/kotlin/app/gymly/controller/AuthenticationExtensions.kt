package app.gymly.controller

import org.springframework.security.core.Authentication

fun Authentication.currentUserId(): Int = name.toIntOrNull() ?: throw IllegalArgumentException("Invalid user id")

fun Authentication.currentRole(): String =
    authorities
        .mapNotNull { it.authority }
        .first { it.startsWith("ROLE_") }
        .removePrefix("ROLE_")
