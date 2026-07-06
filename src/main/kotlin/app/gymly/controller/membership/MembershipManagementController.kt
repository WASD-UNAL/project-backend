package app.gymly.controller.membership

import app.gymly.dto.membership.MembershipResponse
import app.gymly.service.membership.MembershipManagementService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/memberships")
class MembershipManagementController(
    private val membershipManagementService: MembershipManagementService,
) {
    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Int,
    ): ResponseEntity<MembershipResponse> = ResponseEntity.ok(membershipManagementService.getMembershipById(id))

    @GetMapping
    fun getByUserId(
        @RequestParam userId: Int,
    ): ResponseEntity<List<MembershipResponse>> = ResponseEntity.ok(membershipManagementService.getMembershipsByUserId(userId))
}
