package app.gymly.controller.membership

import app.gymly.model.Discount
import app.gymly.service.membership.DiscountManagementService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/membership/discounts")
class DiscountManagementController(private val discountManagementService: DiscountManagementService) {

    @PostMapping
    fun createDiscount(@RequestBody discount: Discount): ResponseEntity<Discount> {
        val createdDiscount = discountManagementService.createDiscount(discount)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDiscount)
    }

    @GetMapping
    fun getAllDiscounts(): ResponseEntity<List<Discount>> {
        val discounts = discountManagementService.getAllDiscounts()
        return ResponseEntity.ok(discounts)
    }

    @PutMapping("/{id}")
    fun updateDiscount(@PathVariable id: Int, @RequestBody discount: Discount): ResponseEntity<Discount> {
        val updated = discountManagementService.updateDiscount(id, discount)
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

