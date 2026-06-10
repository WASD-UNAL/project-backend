package app.gymly.service.membership

import app.gymly.dto.membership.DiscountRequest
import app.gymly.dto.membership.DiscountResponse
import app.gymly.dto.membership.UpdateDiscountRequest
import app.gymly.model.Discount
import app.gymly.repository.DiscountRepository
import org.springframework.stereotype.Service

@Service
class DiscountManagementService(private val discountRepository: DiscountRepository) {

    fun createDiscount(discountRequest: DiscountRequest): DiscountResponse {
        val discount = toEntity(discountRequest)
        val savedDiscount = discountRepository.save(discount)
        return toResponseDTO(savedDiscount)
    }

    fun updateDiscount(id: Int, updateDiscountRequest: UpdateDiscountRequest): DiscountResponse? {
        val existingDiscount = discountRepository.findById(id).orElse(null) ?: return null

        updateDiscountRequest.percentage?.let { existingDiscount.percentage = it }
        updateDiscountRequest.initDate?.let { existingDiscount.initDate = it }
        updateDiscountRequest.endDate?.let { existingDiscount.endDate = it }
        updateDiscountRequest.active?.let { existingDiscount.active = it }

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

    private fun toEntity(discountRequest: DiscountRequest): Discount {
        return Discount(
            id = null,
            percentage = discountRequest.percentage,
            initDate = discountRequest.initDate,
            endDate = discountRequest.endDate,
            active = discountRequest.active
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