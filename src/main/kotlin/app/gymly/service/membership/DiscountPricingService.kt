package app.gymly.service.membership

import app.gymly.model.Discount
import app.gymly.repository.DiscountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class DiscountPricingService(
    private val discountRepository: DiscountRepository,
) {
    @Transactional(readOnly = true)
    fun currentDiscountsByPlan(): Map<Int, Discount> =
        discountRepository
            .findValidWithPlans(LocalDate.now())
            .flatMap { discount -> discount.plans.mapNotNull { plan -> plan.id?.let { it to discount } } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, candidates) -> candidates.maxBy { it.percentage } }

    @Transactional(readOnly = true)
    fun currentDiscountFor(planId: Int): Discount? = currentDiscountsByPlan()[planId]

    fun discountedPrice(
        price: BigDecimal,
        discount: Discount,
    ): BigDecimal =
        price
            .multiply(BigDecimal.ONE.subtract(discount.percentage.movePointLeft(2)))
            .setScale(2, RoundingMode.HALF_UP)
}
