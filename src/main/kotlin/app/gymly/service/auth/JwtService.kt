package app.gymly.service.auth

import app.gymly.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class JwtService(
    private val jwtEncoder: JwtEncoder,
    @Value("\${app.jwt.expiration-minutes}") private val expirationMinutes: Long,
    @Value("\${app.jwt.issuer}") private val issuer: String
) {

    data class IssuedToken(val token: String, val expiresAt: Instant)

    fun issue(user: User, roleName: String): IssuedToken {
        val now = Instant.now()
        val expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES)
        val claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(user.id?.toString() ?: error("User id is null after persistence"))
            .claim("role", roleName.uppercase())
            .claim("document", user.document)
            .claim("email", user.email)
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        return IssuedToken(token, expiresAt)
    }
}
