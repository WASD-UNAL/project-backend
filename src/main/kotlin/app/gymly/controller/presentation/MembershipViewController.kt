package app.gymly.controller.presentation

import app.gymly.controller.currentUserId
import app.gymly.dto.auth.UserResponse
import app.gymly.dto.membership.MyMembershipResponse
import app.gymly.dto.membership.PaymentHistoryItemResponse
import app.gymly.service.presentation.MembershipViewService
import app.gymly.service.presentation.PaymentViewService
import app.gymly.service.presentation.UserProfileService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoints self-service del cliente autenticado (bajo la ruta "/me") para la
 * sección de Pagos. El usuario se resuelve desde el JWT, no por documento.
 */
@RestController
@RequestMapping("/me")
class MembershipViewController(
    private val membershipViewService: MembershipViewService,
    private val paymentViewService: PaymentViewService,
    private val userProfileService: UserProfileService,
) {
    @GetMapping("/profile")
    fun getMyProfile(authentication: Authentication): ResponseEntity<UserResponse> {
        val roleName = (authentication.authorities.first().authority ?: "").removePrefix("ROLE_")
        return ResponseEntity.ok(userProfileService.getMyProfile(authentication.currentUserId(), roleName))
    }

    @GetMapping("/membership")
    fun getMyMembership(authentication: Authentication): ResponseEntity<MyMembershipResponse> =
        ResponseEntity.ok(membershipViewService.getMyMembership(authentication.currentUserId()))

    @GetMapping("/payments")
    fun getMyPayments(authentication: Authentication): ResponseEntity<List<PaymentHistoryItemResponse>> =
        ResponseEntity.ok(paymentViewService.getMyPayments(authentication.currentUserId()))
}
