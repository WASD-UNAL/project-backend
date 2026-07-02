package app.gymly.controller

import org.springframework.security.core.Authentication

/**
 * El subject del JWT es el id del usuario (ver JwtService). Esta extensión lo
 * resuelve de forma consistente para los endpoints self-service bajo "/me".
 */
fun Authentication.currentUserId(): Int = name.toIntOrNull() ?: throw IllegalArgumentException("Invalid user id")
