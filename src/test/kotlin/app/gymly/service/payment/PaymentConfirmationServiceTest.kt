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
    ) = Payment(
        id = 77,
        membershipId = 5,
        userId = 1,
        amount = BigDecimal("90000.00"),
        method = PaymentMethod.CARD,
        status = status,
        reference = reference,
    )

    private fun stubMercadoPago(
        mpStatus: String,
        externalReference: String? = "77",
    ) {
        val mpPayment = mock<MercadoPagoPayment>()
        whenever(mpPayment.status).thenReturn(mpStatus)
        whenever(mpPayment.externalReference).thenReturn(externalReference)
        whenever(mercadoPagoService.getPayment(mpPaymentId)).thenReturn(mpPayment)
    }

    @Nested
    inner class ApprovedPayment {
        @Test
        fun marksPaymentSuccessfulAndActivatesMembership() {
            val payment = localPayment()
            stubMercadoPago("approved")
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)
            whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }

            val result = service.applyMercadoPagoResult(mpPaymentId)

            assertEquals(PaymentStatus.SUCCESSFUL, result.status)
            assertEquals("Inscripción plan Premium", result.reference)
            verify(membershipManagementService).activateMembership(5)
        }

        @Test
        fun fallsBackToMpReferenceWhenPaymentHasNone() {
            val payment = localPayment(reference = null)
            stubMercadoPago("approved")
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)
            whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }

            val result = service.applyMercadoPagoResult(mpPaymentId)

            assertEquals("MP-$mpPaymentId", result.reference)
        }
    }

    @Nested
    inner class RejectedPayment {
        @Test
        fun marksPaymentRejectedAndDeactivatesMembership() {
            val payment = localPayment()
            stubMercadoPago("rejected")
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)
            whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }

            val result = service.applyMercadoPagoResult(mpPaymentId)

            assertEquals(PaymentStatus.REJECTED, result.status)
            verify(membershipManagementService).deactivateMembership(5)
            verify(membershipManagementService, never()).activateMembership(any())
        }
    }

    @Nested
    inner class PendingPayment {
        @Test
        fun keepsPaymentPendingForInProcessStatus() {
            val payment = localPayment()
            stubMercadoPago("in_process")
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)

            val result = service.applyMercadoPagoResult(mpPaymentId)

            assertEquals(PaymentStatus.PENDING, result.status)
            verify(paymentRepository, never()).save(any())
            verify(membershipManagementService, never()).activateMembership(any())
            verify(membershipManagementService, never()).deactivateMembership(any())
        }
    }

    @Nested
    inner class AlreadyConfirmed {
        @Test
        fun isIdempotentForSuccessfulPayments() {
            val payment = localPayment(status = PaymentStatus.SUCCESSFUL)
            stubMercadoPago("approved")
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)

            val result = service.applyMercadoPagoResult(mpPaymentId)

            assertEquals(PaymentStatus.SUCCESSFUL, result.status)
            verify(paymentRepository, never()).save(any())
            verify(membershipManagementService, never()).activateMembership(any())
        }
    }

    @Nested
    inner class Validation {
        @Test
        fun rejectsRequesterWhoDoesNotOwnThePayment() {
            val payment = localPayment()
            stubMercadoPago("approved")
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(payment)

            val error =
                assertThrows(ResponseStatusException::class.java) {
                    service.applyMercadoPagoResult(mpPaymentId, requesterUserId = 99)
                }

            assertEquals(HttpStatus.FORBIDDEN, error.statusCode)
            verify(membershipManagementService, never()).activateMembership(any())
        }

        @Test
        fun failsWhenExternalReferenceIsMissing() {
            stubMercadoPago("approved", externalReference = null)

            val error =
                assertThrows(ResponseStatusException::class.java) {
                    service.applyMercadoPagoResult(mpPaymentId)
                }

            assertEquals(HttpStatus.BAD_REQUEST, error.statusCode)
        }

        @Test
        fun failsWhenLocalPaymentDoesNotExist() {
            stubMercadoPago("approved")
            whenever(paymentRepository.findByIdOrNull(77)).thenReturn(null)

            val error =
                assertThrows(ResponseStatusException::class.java) {
                    service.applyMercadoPagoResult(mpPaymentId)
                }

            assertEquals(HttpStatus.NOT_FOUND, error.statusCode)
        }
    }
}
