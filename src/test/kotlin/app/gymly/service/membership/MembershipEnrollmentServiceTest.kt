package app.gymly.service.membership

import app.gymly.dto.membership.MyMembershipResponse
import app.gymly.exception.NoActiveMembershipException
import app.gymly.model.Membership
import app.gymly.model.MembershipStatus
import app.gymly.model.Payment
import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import app.gymly.model.Plan
import app.gymly.repository.MembershipRepository
import app.gymly.repository.PaymentRepository
import app.gymly.repository.PlanRepository
import app.gymly.service.presentation.MembershipViewService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class MembershipEnrollmentServiceTest {
    private lateinit var membershipRepository: MembershipRepository
    private lateinit var planRepository: PlanRepository
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var membershipViewService: MembershipViewService
    private lateinit var service: MembershipEnrollmentService

    private val plan =
        Plan(
            id = 10,
            name = "Plan Premium",
            durationDays = 30,
            price = BigDecimal("90000.00"),
        )

    private val pendingMembership =
        Membership(
            id = 5,
            userId = 1,
            planId = 10,
            initDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(30),
            status = MembershipStatus.PENDING,
        )

    @BeforeEach
    fun setUp() {
        membershipRepository = mock()
        planRepository = mock()
        paymentRepository = mock()
        membershipViewService = mock()
        service =
            MembershipEnrollmentService(
                membershipRepository,
                planRepository,
                paymentRepository,
                membershipViewService,
            )
        whenever(membershipViewService.getMyMembership(1)).thenReturn(response())
    }

    @Test
    fun updatesTheMethodOfTheExistingPendingPayment() {
        val payment =
            Payment(
                id = 7,
                membershipId = 5,
                userId = 1,
                amount = BigDecimal("90000.00"),
                method = PaymentMethod.CARD,
                status = PaymentStatus.PENDING,
            )
        whenever(membershipRepository.findFirstByUserIdOrderByIdDesc(1)).thenReturn(pendingMembership)
        whenever(planRepository.findByIdOrNull(10)).thenReturn(plan)
        whenever(paymentRepository.findByMembershipId(5)).thenReturn(listOf(payment))

        service.changePaymentMethod(1, PaymentMethod.TRANSFER)

        val captor = argumentCaptor<Payment>()
        verify(paymentRepository).save(captor.capture())
        assertEquals(PaymentMethod.TRANSFER, captor.firstValue.method)
        assertEquals(7, captor.firstValue.id)
    }

    @Test
    fun createsAPendingPaymentWhenNoneExists() {
        whenever(membershipRepository.findFirstByUserIdOrderByIdDesc(1)).thenReturn(pendingMembership)
        whenever(planRepository.findByIdOrNull(10)).thenReturn(plan)
        whenever(paymentRepository.findByMembershipId(5)).thenReturn(emptyList())

        service.changePaymentMethod(1, PaymentMethod.CASH)

        val captor = argumentCaptor<Payment>()
        verify(paymentRepository).save(captor.capture())
        assertEquals(PaymentMethod.CASH, captor.firstValue.method)
        assertEquals(PaymentStatus.PENDING, captor.firstValue.status)
        assertEquals(5, captor.firstValue.membershipId)
    }

    @Test
    fun rejectsChangingMethodWhenThereIsNoPendingMembership() {
        val active = pendingMembership.apply { status = MembershipStatus.ACTIVE }
        whenever(membershipRepository.findFirstByUserIdOrderByIdDesc(1)).thenReturn(active)

        assertThrows<NoActiveMembershipException> {
            service.changePaymentMethod(1, PaymentMethod.CASH)
        }
        verify(paymentRepository, never()).save(org.mockito.kotlin.any())
    }

    private fun response() =
        MyMembershipResponse(
            hasActiveMembership = false,
            pendingApproval = true,
            membershipId = 5,
            planId = 10,
            planName = "Plan Premium",
            price = BigDecimal("90000.00"),
            initDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(30),
            statusColor = "YELLOW",
            daysRemaining = 30,
            message = "Pendiente",
        )
}
