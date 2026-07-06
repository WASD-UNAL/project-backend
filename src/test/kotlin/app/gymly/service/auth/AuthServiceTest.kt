package app.gymly.service.auth

import app.gymly.constants.AppConstants
import app.gymly.dto.auth.LoginRequest
import app.gymly.dto.auth.RefreshTokenRequest
import app.gymly.dto.auth.RegisterRequest
import app.gymly.exception.DocumentAlreadyExistsException
import app.gymly.exception.EmailAlreadyExistsException
import app.gymly.exception.InvalidCredentialsException
import app.gymly.exception.RoleNotConfiguredException
import app.gymly.model.RefreshToken
import app.gymly.model.Role
import app.gymly.model.User
import app.gymly.repository.RefreshTokenRepository
import app.gymly.repository.RoleRepository
import app.gymly.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
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
        testUser =
            User(
                id = 100,
                roleId = 1,
                name = "Test",
                lastname = "User",
                document = "12345678",
                phone = "5551234",
                email = "test@user.com",
                passwordHash = "hashedPassword",
                active = true,
            )
    }

    /**
     * **Funcionalidad Esencial:** Autenticación de usuarios (Login).
     * **Caso Límite:** Intento de inicio de sesión de un usuario que existe en el sistema, pero su cuenta fue desactivada (active = false). Debe denegar el acceso sin llegar a comprobar descifrados de contraseña.
     * **Aislamiento de Base de Datos:** Se mockea `userRepository` para evitar la lectura real a la BD, validando únicamente la lógica de negocio del servicio.
     */
    @Test
    fun `login should throw InvalidCredentialsException when user is not active`() {
        val request = LoginRequest(identifier = "test@user.com", password = "password123")
        val inactiveUser =
            User(
                id = 100,
                roleId = 1,
                name = "Test",
                lastname = "User",
                document = "12345678",
                phone = "555-1234",
                email = "test@user.com",
                passwordHash = "hashedPassword",
                active = false,
            )

        `when`(userRepository.findByEmail("test@user.com")).thenReturn(inactiveUser)

        val exception =
            assertThrows<InvalidCredentialsException> {
                authService.login(request)
            }

        assertEquals("Invalid credentials", exception.message)
        verify(passwordEncoder, never()).matches(anyString(), anyString())
    }

    /**
     * **Funcionalidad Esencial:** Renovación de sesión (Refresh Token).
     * **Caso Límite:** El cliente envía un Refresh Token cuyo tiempo de vida (`expiresAt`) ya ha pasado. La lógica debe capturar esto y abortar el refresh, forzando un re-login.
     * **Aislamiento de Base de Datos:** Se emplea un mock en `refreshTokenRepository` simulando lo que devolvería la consulta, impidiendo cualquier lectura a la base real.
     */
    @Test
    fun `refreshAccessToken should throw InvalidCredentialsException when token is expired`() {
        val expiredTokenStr = "expired-refresh-token"
        val request = RefreshTokenRequest(refreshToken = expiredTokenStr)
        val expiredToken =
            RefreshToken(
                id = 1,
                userId = 100,
                token = expiredTokenStr,
                expiresAt = Instant.now().minus(1, ChronoUnit.DAYS),
                createdAt = Instant.now().minus(8, ChronoUnit.DAYS),
            )

        `when`(refreshTokenRepository.findByToken(expiredTokenStr)).thenReturn(expiredToken)

        val exception =
            assertThrows<InvalidCredentialsException> {
                authService.refreshAccessToken(request)
            }

        assertEquals("Refresh token has expired", exception.message)
        verify(userRepository, never()).findById(anyInt())
    }

    /**
     * **Funcionalidad Esencial:** Rol no existente.
     * **Caso Límite (Out of Bounds):** Se intenta registrar un cliente pero el rol CLIENT
     * no existe en la base de datos (no fue seedeado). El servicio debe abortar con
     * `RoleNotConfiguredException` sin tocar encriptación ni persistencia.
     * **Aislamiento:** Se mockea `roleRepository.findByName` devolviendo null.
     */
    @Test
    fun `registerClient should throw RoleNotConfiguredException when role is not found`() {
        val request =
            RegisterRequest(
                name = "Bob",
                lastname = "NoRole",
                email = "bob@test.com",
                document = "11223344",
                password = "password123",
            )

        `when`(roleRepository.findByName(AppConstants.ROLE_CLIENT)).thenReturn(null)

        assertThrows<RoleNotConfiguredException> {
            authService.registerClient(request)
        }

        verify(passwordEncoder, never()).encode(anyString())
        verify(userRepository, never()).save(any(User::class.java))
    }

    /**
     * **Funcionalidad Esencial:** Unicidad de documento (RF_23).
     * **Caso Límite:** Se intenta registrar un cliente con un documento que ya existe.
     * El servicio debe abortar con `DocumentAlreadyExistsException` sin persistir,
     * en vez de dejar reventar el constraint de la base de datos.
     * **Aislamiento:** Se mockea `userRepository.findByDocument` devolviendo un usuario existente.
     */
    @Test
    fun `registerClient should throw DocumentAlreadyExistsException when document already exists`() {
        val request =
            RegisterRequest(
                name = "Bob",
                lastname = "Dup",
                email = "bob@test.com",
                document = "12345678",
                password = "password123",
            )

        `when`(roleRepository.findByName(AppConstants.ROLE_CLIENT)).thenReturn(testRole)
        `when`(userRepository.findByDocument("12345678")).thenReturn(testUser)

        assertThrows<DocumentAlreadyExistsException> {
            authService.registerClient(request)
        }

        verify(userRepository, never()).save(any(User::class.java))
    }

    /**
     * **Funcionalidad Esencial:** Unicidad de correo.
     * **Caso Límite:** Se intenta registrar un cliente con un correo que ya existe.
     * El servicio debe abortar con `EmailAlreadyExistsException` sin persistir.
     * **Aislamiento:** Se mockea `userRepository.findByEmail` devolviendo un usuario existente.
     */
    @Test
    fun `registerClient should throw EmailAlreadyExistsException when email already exists`() {
        val request =
            RegisterRequest(
                name = "Bob",
                lastname = "Dup",
                email = "test@user.com",
                document = "99887766",
                password = "password123",
            )

        `when`(roleRepository.findByName(AppConstants.ROLE_CLIENT)).thenReturn(testRole)
        `when`(userRepository.findByEmail("test@user.com")).thenReturn(testUser)

        assertThrows<EmailAlreadyExistsException> {
            authService.registerClient(request)
        }

        verify(userRepository, never()).save(any(User::class.java))
    }
}
