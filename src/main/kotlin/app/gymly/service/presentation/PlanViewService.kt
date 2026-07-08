package app.gymly.service.presentation

import app.gymly.dto.membership.PlanDiscountResponse
import app.gymly.dto.membership.PlanResponse
import app.gymly.model.Discount
import app.gymly.model.Plan
import app.gymly.repository.PlanRepository
import app.gymly.service.membership.DiscountPricingService
import org.springframework.stereotype.Service

@Service
class PlanViewService(
    private val planRepository: PlanRepository,
    private val discountPricingService: DiscountPricingService,
) {
    fun getActivePlans(): List<PlanResponse> {
        val discountsByPlan = discountPricingService.currentDiscountsByPlan()
        return planRepository.findByActiveTrue().map { toResponseDTO(it, discountsByPlan[it.id]) }
    }

    fun getPlanById(id: Int): PlanResponse? =
        planRepository
            .findById(id)
            .filter { it.active }
            .map { toResponseDTO(it, discountPricingService.currentDiscountFor(id)) }
            .orElse(null)

    private fun toResponseDTO(
        plan: Plan,
        discount: Discount?,
    ): PlanResponse =
        PlanResponse(
            id = plan.id,
            name = plan.name,
            description = plan.description,
            durationDays = plan.durationDays,
            price = plan.price,
            active = plan.active,
            discount =
                discount?.let {
                    PlanDiscountResponse(
                        id = it.id!!,
                        name = it.name,
                        description = it.description,
                        percentage = it.percentage,
                        discountedPrice = discountPricingService.discountedPrice(plan.price, it),
                        endDate = it.endDate,
                    )
                },
        )
}
