package app.gymly.service.membership

import app.gymly.dto.membership.DiscountCreateDTO
import app.gymly.dto.membership.DiscountResponseDTO
import app.gymly.dto.membership.DiscountUpdateDTO
import app.gymly.model.Discount
import app.gymly.repository.DiscountRepository
import org.springframework.stereotype.Service

@Service
class DiscountManagementService(private val discountRepository: DiscountRepository) {

    fun createDiscount(dto: DiscountCreateDTO): DiscountResponseDTO {
        val discount = toEntity(dto)
        val savedDiscount = discountRepository.save(discount)
        return toResponseDTO(savedDiscount)
    }

    fun updateDiscount(id: Int, dto: DiscountUpdateDTO): DiscountResponseDTO? {
        val existingDiscount = discountRepository.findById(id).orElse(null) ?: return null

        dto.percentage?.let { existingDiscount.percentage = it }
        dto.initDate?.let { existingDiscount.initDate = it }
        dto.endDate?.let { existingDiscount.endDate = it }
        dto.active?.let { existingDiscount.active = it }

        val savedDiscount = discountRepository.save(existingDiscount)
        return toResponseDTO(savedDiscount)
    }

    fun deleteDiscount(id: Int): Boolean {
        return if (discountRepository.existsById(id)) {
            discountRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    fun getAllDiscounts(): List<DiscountResponseDTO> {
        return discountRepository.findAll().map { toResponseDTO(it) }
    }

    private fun toEntity(dto: DiscountCreateDTO): Discount {
        return Discount(
            id = null,
            percentage = dto.percentage,
            initDate = dto.initDate,
            endDate = dto.endDate,
            active = dto.active
        )
    }

    private fun toResponseDTO(entity: Discount): DiscountResponseDTO {
        return DiscountResponseDTO(
            id = entity.id,
            percentage = entity.percentage,
            initDate = entity.initDate,
            endDate = entity.endDate,
            active = entity.active
        )
    }
}