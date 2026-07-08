package app.gymly.service.membership

import app.gymly.dto.membership.DiscountPlanSummary
import app.gymly.dto.membership.DiscountRequest
import app.gymly.dto.membership.DiscountResponse
import app.gymly.dto.membership.UpdateDiscountRequest
import app.gymly.exception.PlanInactiveException
import app.gymly.exception.PlanNotFoundException
import app.gymly.model.Discount
import app.gymly.model.Plan
import app.gymly.repository.DiscountRepository
import app.gymly.repository.PlanRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiscountManagementService(
    private val discountRepository: DiscountRepository,
    private val planRepository: PlanRepository,
) {
    @Transactional
    fun createDiscount(discountRequest: DiscountRequest): DiscountResponse {
        val discount = toEntity(discountRequest, resolveActivePlans(discountRequest.planIds))
        val savedDiscount = discountRepository.save(discount)
        return toResponseDTO(savedDiscount)
    }

    @Transactional
    fun updateDiscount(
        id: Int,
        updateDiscountRequest: UpdateDiscountRequest,
    ): DiscountResponse? {
        val existingDiscount = discountRepository.findById(id).orElse(null) ?: return null

        updateDiscountRequest.name?.let { existingDiscount.name = it }
        updateDiscountRequest.description?.let { existingDiscount.description = it }
        updateDiscountRequest.percentage?.let { existingDiscount.percentage = it }
        updateDiscountRequest.initDate?.let { existingDiscount.initDate = it }
        updateDiscountRequest.endDate?.let { existingDiscount.endDate = it }
        updateDiscountRequest.active?.let { existingDiscount.active = it }
        updateDiscountRequest.planIds?.let { planIds ->
            val plans = resolveActivePlans(planIds)
            existingDiscount.plans.clear()
            existingDiscount.plans.addAll(plans)
        }

        val savedDiscount = discountRepository.save(existingDiscount)
        return toResponseDTO(savedDiscount)
    }

    @Transactional
    fun deleteDiscount(id: Int): Boolean {
        if (!discountRepository.existsById(id)) return false
        discountRepository.deleteById(id)
        return true
    }

    @Transactional(readOnly = true)
    fun getAllDiscounts(): List<DiscountResponse> = discountRepository.findAllWithPlans().map { toResponseDTO(it) }

    private fun resolveActivePlans(planIds: List<Int>): Set<Plan> {
        val uniqueIds = planIds.distinct()
        val plans = planRepository.findAllById(uniqueIds)
        val foundIds = plans.mapNotNull { it.id }.toSet()
        uniqueIds.firstOrNull { it !in foundIds }?.let { throw PlanNotFoundException(it) }
        plans.firstOrNull { !it.active }?.let { throw PlanInactiveException(it.id!!) }
        return plans.toSet()
    }

    private fun toEntity(
        discountRequest: DiscountRequest,
        plans: Set<Plan>,
    ): Discount =
        Discount(
            id = null,
            name = discountRequest.name,
            description = discountRequest.description,
            percentage = discountRequest.percentage,
            initDate = discountRequest.initDate,
            endDate = discountRequest.endDate,
            active = discountRequest.active,
            plans = plans.toMutableSet(),
        )

    private fun toResponseDTO(discount: Discount): DiscountResponse =
        DiscountResponse(
            id = discount.id,
            name = discount.name,
            description = discount.description,
            percentage = discount.percentage,
            initDate = discount.initDate,
            endDate = discount.endDate,
            active = discount.active,
            plans =
                discount.plans
                    .sortedBy { it.id }
                    .map { DiscountPlanSummary(id = it.id!!, name = it.name) },
        )
}
