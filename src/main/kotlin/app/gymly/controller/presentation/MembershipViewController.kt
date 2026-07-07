package app.gymly.controller.presentation

import app.gymly.controller.currentRole
import app.gymly.controller.currentUserId
import app.gymly.dto.auth.ChangePasswordRequest
import app.gymly.dto.auth.UpdateProfileRequest
import app.gymly.dto.auth.UserResponse
import app.gymly.dto.membership.MyMembershipResponse
import app.gymly.dto.membership.PaymentHistoryItemResponse
import app.gymly.service.presentation.MembershipViewService
import app.gymly.service.presentation.PaymentViewService
import app.gymly.service.presentation.UserProfileService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me")
class MembershipViewController(
    private val membershipViewService: MembershipViewService,
    private val paymentViewService: PaymentViewService,
    private val userProfileService: UserProfileService,
) {
    @GetMapping("/profile")
    fun getMyProfile(authentication: Authentication): ResponseEntity<UserResponse> =
        ResponseEntity.ok(
            userProfileService.getMyProfile(authentication.currentUserId(), authentication.currentRole()),
        )

    @PutMapping("/profile")
    fun updateMyProfile(
        authentication: Authentication,
        @Valid @RequestBody request: UpdateProfileRequest,
    ): ResponseEntity<UserResponse> =
        ResponseEntity.ok(
            userProfileService.updateMyProfile(authentication.currentUserId(), authentication.currentRole(), request),
        )

    @PutMapping("/password")
    fun changeMyPassword(
        authentication: Authentication,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        userProfileService.changeMyPassword(authentication.currentUserId(), request)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/membership")
    fun getMyMembership(authentication: Authentication): ResponseEntity<MyMembershipResponse> =
        ResponseEntity.ok(membershipViewService.getMyMembership(authentication.currentUserId()))

    @GetMapping("/payments")
    fun getMyPayments(authentication: Authentication): ResponseEntity<List<PaymentHistoryItemResponse>> =
        ResponseEntity.ok(paymentViewService.getMyPayments(authentication.currentUserId()))
}
