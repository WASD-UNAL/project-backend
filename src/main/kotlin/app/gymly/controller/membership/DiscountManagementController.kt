package app.gymly.controller.membership

import app.gymly.dto.membership.DiscountCreate
import app.gymly.dto.membership.DiscountResponse
import app.gymly.dto.membership.DiscountUpdate
import app.gymly.service.membership.DiscountManagementService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/membership/discounts")
class DiscountManagementController(private val discountManagementService: DiscountManagementService) {

    @PostMapping
    fun createDiscount(@Valid @RequestBody dto: DiscountCreate): ResponseEntity<DiscountResponse> {
        val createdDiscount = discountManagementService.createDiscount(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDiscount)
    }

    @GetMapping
    fun getAllDiscounts(): ResponseEntity<List<DiscountResponse>> {
        val discounts = discountManagementService.getAllDiscounts()
        return ResponseEntity.ok(discounts)
    }

    @PutMapping("/{id}")
    fun updateDiscount(
        @PathVariable id: Int,
        @Valid @RequestBody dto: DiscountUpdate
    ): ResponseEntity<DiscountResponse> {
        val updated = discountManagementService.updateDiscount(id, dto)
        return if (updated != null) {
            ResponseEntity.ok(updated)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteDiscount(@PathVariable id: Int): ResponseEntity<Unit> {
        val deleted = discountManagementService.deleteDiscount(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

