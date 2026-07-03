package app.gymly.controller

import org.springframework.security.core.Authentication

fun Authentication.currentUserId(): Int = name.toIntOrNull() ?: throw IllegalArgumentException("Invalid user id")
