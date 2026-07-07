package app.gymly.controller.payment

import app.gymly.constants.AppConstants
import app.gymly.controller.currentUserId
import app.gymly.dto.payment.CheckoutResponse
import app.gymly.dto.payment.ConfirmCheckoutRequest
import app.gymly.dto.payment.ConfirmCheckoutResponse
import app.gymly.dto.payment.PaymentRequest
import app.gymly.dto.payment.PaymentResponse
import app.gymly.dto.payment.UpdatePaymentRequest
import app.gymly.service.payment.PaymentConfirmationService
import app.gymly.service.payment.PaymentManagementService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/payments")
class PaymentManagementController(
    private val paymentService: PaymentManagementService,
    private val paymentConfirmationService: PaymentConfirmationService,
) {
    @GetMapping
    @PreAuthorize("hasRole('${AppConstants.ROLE_ADMIN_UPPER}')")
    fun getAllPayments(): ResponseEntity<List<PaymentResponse>> = ResponseEntity.ok(paymentService.getAllPayments())

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('${AppConstants.ROLE_ADMIN_UPPER}')")
    fun getPaymentById(
        @PathVariable id: Int,
    ): ResponseEntity<PaymentResponse> = ResponseEntity.ok(paymentService.getPaymentById(id))

    @PostMapping
    @PreAuthorize("hasRole('${AppConstants.ROLE_ADMIN_UPPER}')")
    fun createPayment(
        @Valid @RequestBody request: PaymentRequest,
    ): ResponseEntity<PaymentResponse> {
        val createdPayment = paymentService.createPayment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment)
    }

    @PostMapping("/checkout")
    fun createCheckout(
        @Valid @RequestBody paymentRequest: PaymentRequest,
    ): ResponseEntity<CheckoutResponse> {
        val checkoutResponse = paymentService.createCheckout(paymentRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(checkoutResponse)
    }

    @PostMapping("/checkout/confirm")
    fun confirmCheckout(
        @Valid @RequestBody request: ConfirmCheckoutRequest,
        authentication: Authentication,
    ): ResponseEntity<ConfirmCheckoutResponse> {
        val payment =
            paymentConfirmationService.applyMercadoPagoResult(
                request.mpPaymentId!!,
                authentication.currentUserId(),
            )
        return ResponseEntity.ok(ConfirmCheckoutResponse(paymentId = payment.id ?: 0, status = payment.status))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('${AppConstants.ROLE_ADMIN_UPPER}')")
    fun updatePayment(
        @PathVariable id: Int,
        @Valid @RequestBody request: UpdatePaymentRequest,
    ): ResponseEntity<PaymentResponse> = ResponseEntity.ok(paymentService.updatePayment(id, request))

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('${AppConstants.ROLE_ADMIN_UPPER}')")
    fun deletePayment(
        @PathVariable id: Int,
    ): ResponseEntity<Unit> {
        paymentService.deletePayment(id)
        return ResponseEntity.noContent().build()
    }
}
