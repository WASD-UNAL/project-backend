package app.gymly.controller.membership

import app.gymly.dto.membership.DiscountCreateDTO
import app.gymly.dto.membership.DiscountResponseDTO
import app.gymly.dto.membership.DiscountUpdateDTO
import app.gymly.service.membership.DiscountManagementService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/membership/discounts")
class DiscountManagementController(private val discountManagementService: DiscountManagementService) {

    @PostMapping
    fun createDiscount(@Valid @RequestBody dto: DiscountCreateDTO): ResponseEntity<DiscountResponseDTO> {
        val createdDiscount = discountManagementService.createDiscount(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDiscount)
    }

    @GetMapping
    fun getAllDiscounts(): ResponseEntity<List<DiscountResponseDTO>> {
        val discounts = discountManagementService.getAllDiscounts()
        return ResponseEntity.ok(discounts)
    }

    @PutMapping("/{id}")
    fun updateDiscount(
        @PathVariable id: Int,
        @Valid @RequestBody dto: DiscountUpdateDTO
    ): ResponseEntity<DiscountResponseDTO> {
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

