package app.gymly.service.payment

import app.gymly.model.Payment
import app.gymly.model.PaymentMethod
import app.gymly.model.PaymentStatus
import app.gymly.repository.PaymentRepository
import app.gymly.service.membership.MembershipManagementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.OffsetDateTime
import com.mercadopago.resources.payment.Payment as MercadoPagoPayment

class PaymentConfirmationServiceTest {
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var membershipManagementService: MembershipManagementService
    private lateinit var mercadoPagoService: MercadoPagoService
    private lateinit var service: PaymentConfirmationService

    private val mpPaymentId = 123456789L

    @BeforeEach
    fun setUp() {
        paymentRepository = mock()
        membershipManagementService = mock()
        mercadoPagoService = mock()
        service = PaymentConfirmationService(paymentRepository, membershipManagementService, mercadoPagoService)
    }

    private fun localPayment(
        status: PaymentStatus = PaymentStatus.PENDING,
        reference: String? = "Inscripción plan Premium",
        createdAt: OffsetDateTime? = OffsetDateTime.now(),
    ) = Payment(
        id = 77,
        membershipId = 5,
        userId = 1,
        amount = BigDecimal("90000.00"),
        method = PaymentMethod.CARD,
        status = status,
        reference = reference,
        createdAt = createdAt,
    )

    private fun mpPayment(
        status: String,
        externalReference: String? = "77",
        id: Long = mpPaymentId,
    ): MercadoPagoPayment {
        val mp = mock<MercadoPagoPayment>()
        whenever(mp.status).thenReturn(status)
        whenever(mp.externalReference).thenReturn(externalReference)
        whenever(mp.id).thenReturn(id)
        return mp
    }

    @Nested
    inner class ApplyMercadoPagoResult {
        @Test
        fun approvesAndActivatesWhenMpApproved() {
            val payment = localPayment()
            whenever(mercadoPagoService.getPayment(mpPaymentId)).thenReturn(mpPayment("approved"))
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)
            whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }

            val result = service.applyMercadoPagoResult(mpPaymentId)

            assertEquals(PaymentStatus.SUCCESSFUL, result.status)
            verify(membershipManagementService).activateMembership(5)
        }

        @Test
        fun leavesPendingWhenMpRejectedToRespectRetryGrace() {
            val payment = localPayment()
            whenever(mercadoPagoService.getPayment(mpPaymentId)).thenReturn(mpPayment("rejected"))
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)

            val result = service.applyMercadoPagoResult(mpPaymentId)

            assertEquals(PaymentStatus.PENDING, result.status)
            verify(paymentRepository, never()).save(any())
            verify(membershipManagementService, never()).deactivateMembership(any())
        }

        @Test
        fun rejectsRequesterWhoDoesNotOwnThePayment() {
            val payment = localPayment()
            whenever(mercadoPagoService.getPayment(mpPaymentId)).thenReturn(mpPayment("approved"))
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)

            val error =
                assertThrows(ResponseStatusException::class.java) {
                    service.applyMercadoPagoResult(mpPaymentId, requesterUserId = 99)
                }

            assertEquals(HttpStatus.FORBIDDEN, error.statusCode)
            verify(membershipManagementService, never()).activateMembership(any())
        }
    }

    @Nested
    inner class ReconcilePendingCardPayment {
        @Test
        fun approvesWhenThereIsAnApprovedAttempt() {
            val payment = localPayment()
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)
            whenever(mercadoPagoService.searchPaymentsByExternalReference("77"))
                .thenReturn(listOf(mpPayment("rejected", id = 111L), mpPayment("approved", id = 222L)))
            whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }

            service.reconcilePendingCardPayment(77)

            assertEquals(PaymentStatus.SUCCESSFUL, payment.status)
            verify(membershipManagementService).activateMembership(5)
        }

        @Test
        fun keepsPendingWithinGraceWhenNotApproved() {
            val payment = localPayment(createdAt = OffsetDateTime.now().minusMinutes(1))
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)
            whenever(mercadoPagoService.searchPaymentsByExternalReference("77"))
                .thenReturn(listOf(mpPayment("rejected", id = 111L)))

            service.reconcilePendingCardPayment(77)

            assertEquals(PaymentStatus.PENDING, payment.status)
            verify(paymentRepository, never()).save(any())
            verify(membershipManagementService, never()).deactivateMembership(any())
        }

        @Test
        fun rejectsAfterGraceWhenNotApproved() {
            val payment = localPayment(createdAt = OffsetDateTime.now().minusMinutes(5))
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)
            whenever(mercadoPagoService.searchPaymentsByExternalReference("77"))
                .thenReturn(listOf(mpPayment("rejected", id = 111L)))
            whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }

            service.reconcilePendingCardPayment(77)

            assertEquals(PaymentStatus.REJECTED, payment.status)
            verify(membershipManagementService).deactivateMembership(5)
        }

        @Test
        fun ignoresNonCardOrNonPendingPayments() {
            val payment = localPayment(status = PaymentStatus.SUCCESSFUL)
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)

            service.reconcilePendingCardPayment(77)

            verify(mercadoPagoService, never()).searchPaymentsByExternalReference(any())
            verify(paymentRepository, never()).save(any())
        }
    }
}
