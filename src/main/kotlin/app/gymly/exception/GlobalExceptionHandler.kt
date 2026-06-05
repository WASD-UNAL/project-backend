package app.gymly.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException::class, BadCredentialsException::class)
    fun handleInvalidCredentials(e: RuntimeException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("invalid_credentials", e.message))

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(error("forbidden", e.message))

    @ExceptionHandler(EmailAlreadyExistsException::class, DocumentAlreadyExistsException::class)
    fun handleDuplicate(e: RuntimeException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(error("conflict", e.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any?>> {
        val fields = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to "validation_failed", "fields" to fields))
    }

    @ExceptionHandler(RoleNotConfiguredException::class)
    fun handleMisconfiguration(e: RoleNotConfiguredException): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("server_misconfigured", e.message))

    private fun error(code: String, message: String?): Map<String, Any?> =
        mapOf("error" to code, "message" to (message ?: code))
}
