package app.gymly.controller.membership

import app.gymly.controller.currentUserId
import app.gymly.dto.membership.ChangePaymentMethodRequest
import app.gymly.dto.membership.EnrollRequest
import app.gymly.dto.membership.MyMembershipResponse
import app.gymly.service.membership.MembershipEnrollmentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me/membership")
class MembershipEnrollmentController(
    private val membershipEnrollmentService: MembershipEnrollmentService,
) {
    @PostMapping("/enroll")
    fun enroll(
        authentication: Authentication,
        @Valid @RequestBody request: EnrollRequest,
    ): ResponseEntity<MyMembershipResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(membershipEnrollmentService.enroll(authentication.currentUserId(), request))

    @PatchMapping("/payment-method")
    fun changePaymentMethod(
        authentication: Authentication,
        @Valid @RequestBody request: ChangePaymentMethodRequest,
    ): ResponseEntity<MyMembershipResponse> =
        ResponseEntity.ok(
            membershipEnrollmentService.changePaymentMethod(
                authentication.currentUserId(),
                request.paymentMethod!!,
            ),
        )

    @PatchMapping("/cancel")
    fun cancel(authentication: Authentication): ResponseEntity<MyMembershipResponse> =
        ResponseEntity.ok(membershipEnrollmentService.cancel(authentication.currentUserId()))
}
