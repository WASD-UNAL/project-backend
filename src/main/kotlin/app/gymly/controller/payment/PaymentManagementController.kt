package app.gymly.controller.payment

import app.gymly.dto.payment.CheckoutResponse
import app.gymly.dto.payment.PaymentRequest
import app.gymly.dto.payment.PaymentResponse
import app.gymly.dto.payment.UpdatePaymentRequest
import app.gymly.service.payment.PaymentManagementService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/payments")
class PaymentManagementController(
    private val paymentService: PaymentManagementService,
) {
    @GetMapping
    fun getAllPayments(): ResponseEntity<List<PaymentResponse>> = ResponseEntity.ok(paymentService.getAllPayments())

    @GetMapping("/{id}")
    fun getPaymentById(
        @PathVariable id: Int,
    ): ResponseEntity<PaymentResponse> = ResponseEntity.ok(paymentService.getPaymentById(id))

    @PostMapping
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

    @PutMapping("/{id}")
    fun updatePayment(
        @PathVariable id: Int,
        @Valid @RequestBody request: UpdatePaymentRequest,
    ): ResponseEntity<PaymentResponse> = ResponseEntity.ok(paymentService.updatePayment(id, request))

    @DeleteMapping("/{id}")
    fun deletePayment(
        @PathVariable id: Int,
    ): ResponseEntity<Unit> {
        paymentService.deletePayment(id)
        return ResponseEntity.noContent().build()
    }
}
