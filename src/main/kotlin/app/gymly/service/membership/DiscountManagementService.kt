package app.gymly.service.membership

import app.gymly.model.Discount
import app.gymly.repository.DiscountRepository
import org.springframework.stereotype.Service

@Service
class DiscountManagementService(private val discountRepository: DiscountRepository) {

    fun createDiscount(discount: Discount): Discount {
        return discountRepository.save(discount)
    }

    fun updateDiscount(id: Int, updatedDiscount: Discount): Discount? {
        if (!discountRepository.existsById(id)) return null

        val discountToSave = Discount(
            id = id,
            percentage = updatedDiscount.percentage,
            initDate = updatedDiscount.initDate,
            endDate = updatedDiscount.endDate,
            active = updatedDiscount.active
        )
        return discountRepository.save(discountToSave)
    }

    fun deleteDiscount(id: Int): Boolean {
        return if (discountRepository.existsById(id)) {
            discountRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    fun getAllDiscounts(): List<Discount> {
        return discountRepository.findAll()
    }
}

