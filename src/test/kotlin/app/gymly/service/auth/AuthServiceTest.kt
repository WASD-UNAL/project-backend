package app.gymly.service.auth

import app.gymly.constants.AppConstants
import app.gymly.dto.auth.LoginRequest
import app.gymly.dto.auth.RefreshTokenRequest
import app.gymly.dto.auth.RegisterRequest
import app.gymly.exception.InvalidCredentialsException
import app.gymly.model.RefreshToken
import app.gymly.model.Role
import app.gymly.model.User
import app.gymly.repository.RefreshTokenRepository
import app.gymly.repository.RoleRepository
import app.gymly.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var roleRepository: RoleRepository

    @Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtService: JwtService

    @InjectMocks
    private lateinit var authService: AuthService

    private lateinit var testUser: User
    private lateinit var testRole: Role

    @BeforeEach
    fun setup() {
        testRole = Role(id = 1, name = AppConstants.ROLE_CLIENT)
        testUser = User(
            id = 100,
            roleId = 1,
            name = "Test",
            lastname = "User",
            document = "12345678",
            phone = "5551234",
            email = "test@user.com",
            passwordHash = "hashedPassword",
            active = true
        )
    }

    @Test
    fun `login should throw InvalidCredentialsException when user is not active`() {
        val request = LoginRequest(identifier = "test@user.com", password = "password123")
        val inactiveUser = User(
            id = 100, roleId = 1, name = "Test", lastname = "User",
            document = "12345678", phone = "555-1234", email = "test@user.com",
            passwordHash = "hashedPassword", active = false
        )

        `when`(userRepository.findByEmail("test@user.com")).thenReturn(inactiveUser)

        val exception = assertThrows<InvalidCredentialsException> {
            authService.login(request)
        }

        assertEquals("Invalid credentials", exception.message)
        verify(passwordEncoder, never()).matches(anyString(), anyString())
    }

    @Test
    fun `refreshAccessToken should throw InvalidCredentialsException when token is expired`() {
        val expiredTokenStr = "expired-refresh-token"
        val request = RefreshTokenRequest(refreshToken = expiredTokenStr)
        val expiredToken = RefreshToken(
            id = 1,
            userId = 100,
            token = expiredTokenStr,
            expiresAt = Instant.now().minus(1, ChronoUnit.DAYS),
            createdAt = Instant.now().minus(8, ChronoUnit.DAYS)
        )

        `when`(refreshTokenRepository.findByToken(expiredTokenStr)).thenReturn(expiredToken)

        val exception = assertThrows<InvalidCredentialsException> {
            authService.refreshAccessToken(request)
        }

        assertEquals("Refresh token has expired", exception.message)
        verify(userRepository, never()).findById(anyInt())
    }

    @Test
    fun `registerClient should save user and return mapped AuthResponse`() {
        val request = RegisterRequest(
            name = "New",
            lastname = "Client",
            email = "new@client.com",
            document = "87654321",
            password = "securePassword"
        )

        `when`(roleRepository.findByName(AppConstants.ROLE_CLIENT)).thenReturn(testRole)
        `when`(passwordEncoder.encode("securePassword")).thenReturn("encodedPassword")

        val savedUser = User(
            id = 200,
            roleId = 1,
            name = "New",
            lastname = "Client",
            email = "new@client.com",
            document = "87654321",
            passwordHash = "encodedPassword",
            active = true
        )

        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)

        val issuedTokens = JwtService.IssuedToken(
            token = "jwt-access-token",
            expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES),
            refreshToken = "jwt-refresh-token",
            refreshExpiresAt = Instant.now().plus(7, ChronoUnit.DAYS)
        )

        `when`(jwtService.issue(savedUser, AppConstants.ROLE_CLIENT)).thenReturn(issuedTokens)
        `when`(refreshTokenRepository.save(any(RefreshToken::class.java))).thenAnswer { it.arguments[0] }

        val response = authService.registerClient(request)


        assertNotNull(response)
        assertEquals("jwt-access-token", response.token)
        assertEquals("jwt-refresh-token", response.refreshToken)
        assertEquals("New", response.user.name)
        assertEquals(AppConstants.ROLE_CLIENT, response.user.role)

        verify(roleRepository).findByName(AppConstants.ROLE_CLIENT)
        verify(userRepository).save(any(User::class.java))
        verify(jwtService).issue(savedUser, AppConstants.ROLE_CLIENT)
        verify(refreshTokenRepository).save(any(RefreshToken::class.java))
    }
}




