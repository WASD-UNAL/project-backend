package app.gymly.service.membership

import app.gymly.dto.membership.DiscountCreate
import app.gymly.dto.membership.DiscountResponse
import app.gymly.dto.membership.DiscountUpdate
import app.gymly.model.Discount
import app.gymly.repository.DiscountRepository
import org.springframework.stereotype.Service

@Service
class DiscountManagementService(private val discountRepository: DiscountRepository) {

    fun createDiscount(discountCreate: DiscountCreate): DiscountResponse {
        val discount = toEntity(discountCreate)
        val savedDiscount = discountRepository.save(discount)
        return toResponseDTO(savedDiscount)
    }

    fun updateDiscount(id: Int, discountUpdate: DiscountUpdate): DiscountResponse? {
        val existingDiscount = discountRepository.findById(id).orElse(null) ?: return null

        discountUpdate.percentage?.let { existingDiscount.percentage = it }
        discountUpdate.initDate?.let { existingDiscount.initDate = it }
        discountUpdate.endDate?.let { existingDiscount.endDate = it }
        discountUpdate.active?.let { existingDiscount.active = it }

        val savedDiscount = discountRepository.save(existingDiscount)
        return toResponseDTO(savedDiscount)
    }

    fun deleteDiscount(id: Int): Boolean {
        if (!discountRepository.existsById(id)) return false
        discountRepository.deleteById(id)
        return true
    }

    fun getAllDiscounts(): List<DiscountResponse> {
        return discountRepository.findAll().map { toResponseDTO(it) }
    }

    private fun toEntity(discountCreate: DiscountCreate): Discount {
        return Discount(
            id = null,
            percentage = discountCreate.percentage,
            initDate = discountCreate.initDate,
            endDate = discountCreate.endDate,
            active = discountCreate.active
        )
    }

    private fun toResponseDTO(discount: Discount): DiscountResponse {
        return DiscountResponse(
            id = discount.id,
            percentage = discount.percentage,
            initDate = discount.initDate,
            endDate = discount.endDate,
            active = discount.active
        )
    }
}