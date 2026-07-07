package app.gymly.service.payment

import app.gymly.dto.payment.PaymentRequest
import app.gymly.model.Membership
import app.gymly.model.MembershipStatus
import app.gymly.model.Payment
import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import app.gymly.model.Plan
import app.gymly.repository.MembershipRepository
import app.gymly.repository.PaymentRepository
import app.gymly.repository.PlanRepository
import app.gymly.service.membership.MembershipManagementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class PaymentManagementServiceTest {
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var membershipManagementService: MembershipManagementService
    private lateinit var membershipRepository: MembershipRepository
    private lateinit var planRepository: PlanRepository
    private lateinit var mercadoPagoService: MercadoPagoService
    private lateinit var service: PaymentManagementService

    private val plan =
        Plan(
            id = 10,
            name = "Plan Premium",
            durationDays = 30,
            price = BigDecimal("90000.00"),
        )

    private val membership =
        Membership(
            id = 5,
            userId = 1,
            planId = 10,
            initDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(30),
            status = MembershipStatus.PENDING,
        )

    private fun checkoutRequest() =
        PaymentRequest(
            membershipId = 5,
            userId = 1,
            amount = BigDecimal("90000.00"),
            method = PaymentMethod.CARD,
            status = PaymentStatus.PENDING,
        )

    @BeforeEach
    fun setUp() {
        paymentRepository = mock()
        membershipManagementService = mock()
        membershipRepository = mock()
        planRepository = mock()
        mercadoPagoService = mock()
        service =
            PaymentManagementService(
                paymentRepository,
                membershipManagementService,
                membershipRepository,
                planRepository,
                mercadoPagoService,
            )

        whenever(membershipRepository.findByIdOrNull(5)).thenReturn(membership)
        whenever(planRepository.findByIdOrNull(10)).thenReturn(plan)
        whenever(mercadoPagoService.createPaymentLink(any(), any(), any()))
            .thenReturn("https://mp.test/checkout")
    }

    @Nested
    inner class CreateCheckout {
        @Test
        fun newPaymentGetsPlanNameAsReference() {
            whenever(paymentRepository.findByMembershipId(5)).thenReturn(emptyList())
            whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }

            service.createCheckout(checkoutRequest())

            val captor = argumentCaptor<Payment>()
            verify(paymentRepository).save(captor.capture())
            assertEquals("Inscripción plan Plan Premium", captor.firstValue.reference)
            assertEquals(PaymentStatus.PENDING, captor.firstValue.status)
        }

        @Test
        fun reusedPendingPaymentGetsPlanNameAsReference() {
            val pending =
                Payment(
                    id = 77,
                    membershipId = 5,
                    userId = 1,
                    amount = BigDecimal("90000.00"),
                    method = PaymentMethod.CARD,
                    status = PaymentStatus.PENDING,
                    reference = null,
                )
            whenever(paymentRepository.findByMembershipId(5)).thenReturn(listOf(pending))
            whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }

            val response = service.createCheckout(checkoutRequest())

            assertEquals(77, response.paymentId)
            assertEquals("Inscripción plan Plan Premium", pending.reference)
        }

        @Test
        fun checkoutUsesPaymentIdAsExternalReference() {
            val pending =
                Payment(
                    id = 77,
                    membershipId = 5,
                    userId = 1,
                    amount = BigDecimal("90000.00"),
                    method = PaymentMethod.CARD,
                    status = PaymentStatus.PENDING,
                )
            whenever(paymentRepository.findByMembershipId(5)).thenReturn(listOf(pending))
            whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }

            val response = service.createCheckout(checkoutRequest())

            verify(mercadoPagoService).createPaymentLink(
                eq("You are paying: Plan Premium"),
                eq(BigDecimal("90000.00")),
                eq("77"),
            )
            assertEquals("https://mp.test/checkout", response.checkoutUrl)
        }
    }
}
