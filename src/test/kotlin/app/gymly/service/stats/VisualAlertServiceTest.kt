package app.gymly.service.stats

import app.gymly.model.Membership
import app.gymly.model.MembershipStatus
import app.gymly.model.User
import app.gymly.repository.MembershipRepository
import app.gymly.repository.UserRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class VisualAlertServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var membershipRepository: MembershipRepository
    private lateinit var service: VisualAlertService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        membershipRepository = mock()
        service = VisualAlertService(userRepository, membershipRepository)
    }

    private fun buildUser(
        id: Int? = 1,
        name: String = "Juan",
        lastname: String = "Perez",
        document: String = "1001",
    ) = User(
        id = id,
        roleId = 1,
        name = name,
        lastname = lastname,
        email = "juan@gymly.app",
        passwordHash = "hash",
        document = document,
    )

    private fun buildMembership(
        endDate: LocalDate,
        status: MembershipStatus = MembershipStatus.ACTIVE,
        userId: Int = 1,
    ) = Membership(
        id = 1,
        userId = userId,
        planId = 1,
        initDate = LocalDate.now().minusMonths(1),
        endDate = endDate,
        status = status,
    )

    @Nested
    inner class CheckAccessColor {
        @Test
        fun returnsRedWhenUserDoesNotExist() {
            val document = "9999"
            whenever(userRepository.findByDocument(document)).thenReturn(null)

            val result = service.checkAccessColor(document)

            Assertions.assertEquals("RED", result.statusColor)
            Assertions.assertEquals(document, result.document)
            Assertions.assertNull(result.userName)
            Assertions.assertEquals(0L, result.daysRemaining)
            Assertions.assertEquals("Usuario no registrado en el sistema.", result.message)
            verify(membershipRepository, never()).findFirstByUserIdOrderByIdDesc(any())
        }
    }

    @Nested
    inner class GetLatestMembership {
        @Test
        fun returnsRedWhenUserHasNoMembership() {
            val user = buildUser()
            whenever(userRepository.findByDocument(user.document)).thenReturn(user)
            whenever(membershipRepository.findFirstByUserIdOrderByIdDesc(user.id!!)).thenReturn(null)

            val result = service.checkAccessColor(user.document)

            Assertions.assertEquals("RED", result.statusColor)
            Assertions.assertEquals("El usuario no cuenta con ninguna membresía.", result.message)
            Assertions.assertEquals(0L, result.daysRemaining)
            Assertions.assertEquals(user.id, result.userId)
        }
    }

    @Nested
    inner class EvaluateMembershipStatus {
        @Test
        fun endDateTodayIsStillValid() {
            val user = buildUser()
            val membership = buildMembership(endDate = LocalDate.now())
            whenever(userRepository.findByDocument(user.document)).thenReturn(user)
            whenever(membershipRepository.findFirstByUserIdOrderByIdDesc(user.id!!)).thenReturn(membership)

            val result = service.checkAccessColor(user.document)

            Assertions.assertEquals("YELLOW", result.statusColor)
            Assertions.assertEquals(0L, result.daysRemaining)
        }

        @Test
        fun endDateYesterdayIsExpired() {
            val user = buildUser()
            val yesterday = LocalDate.now().minusDays(1)
            val membership = buildMembership(endDate = yesterday)
            whenever(userRepository.findByDocument(user.document)).thenReturn(user)
            whenever(membershipRepository.findFirstByUserIdOrderByIdDesc(user.id!!)).thenReturn(membership)

            val result = service.checkAccessColor(user.document)

            Assertions.assertEquals("RED", result.statusColor)
            Assertions.assertEquals(0L, result.daysRemaining)
            Assertions.assertEquals("Membresía vencida el $yesterday.", result.message)
        }

        @Test
        fun nonActiveStatusOverridesFutureDate() {
            val user = buildUser()
            val membership =
                buildMembership(
                    endDate = LocalDate.now().plusDays(30),
                    status = MembershipStatus.FROZEN,
                )
            whenever(userRepository.findByDocument(user.document)).thenReturn(user)
            whenever(membershipRepository.findFirstByUserIdOrderByIdDesc(user.id!!)).thenReturn(membership)

            val result = service.checkAccessColor(user.document)

            Assertions.assertEquals("RED", result.statusColor)
            Assertions.assertEquals("Membresía inactiva (Estado: FROZEN).", result.message)
        }
    }
}
