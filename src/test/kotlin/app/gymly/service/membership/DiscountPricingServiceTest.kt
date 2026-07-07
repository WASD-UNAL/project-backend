package app.gymly.service.membership

import app.gymly.model.Discount
import app.gymly.model.Plan
import app.gymly.repository.DiscountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class DiscountPricingServiceTest {
    private lateinit var discountRepository: DiscountRepository
    private lateinit var service: DiscountPricingService

    private val monthlyPlan = Plan(id = 1, name = "Mensual", durationDays = 30, price = BigDecimal("90000.00"))
    private val yearlyPlan = Plan(id = 2, name = "Anual", durationDays = 365, price = BigDecimal("900000.00"))

    private fun discount(
        id: Int,
        percentage: String,
        vararg plans: Plan,
    ) = Discount(
        id = id,
        name = "Promo $id",
        percentage = BigDecimal(percentage),
        initDate = LocalDate.now().minusDays(1),
        endDate = LocalDate.now().plusDays(10),
        active = true,
        plans = plans.toMutableSet(),
    )

    @BeforeEach
    fun setUp() {
        discountRepository = mock()
        service = DiscountPricingService(discountRepository)
    }

    @Test
    fun mapsEachPlanToItsDiscount() {
        whenever(discountRepository.findValidWithPlans(any()))
            .thenReturn(listOf(discount(1, "20.00", monthlyPlan, yearlyPlan)))

        val byPlan = service.currentDiscountsByPlan()

        assertEquals(1, byPlan[1]?.id)
        assertEquals(1, byPlan[2]?.id)
    }

    @Test
    fun picksHighestPercentageWhenPlanHasSeveralDiscounts() {
        whenever(discountRepository.findValidWithPlans(any()))
            .thenReturn(
                listOf(
                    discount(1, "10.00", monthlyPlan),
                    discount(2, "25.00", monthlyPlan),
                ),
            )

        assertEquals(2, service.currentDiscountFor(1)?.id)
    }

    @Test
    fun returnsNullForPlanWithoutDiscount() {
        whenever(discountRepository.findValidWithPlans(any()))
            .thenReturn(listOf(discount(1, "20.00", monthlyPlan)))

        assertNull(service.currentDiscountFor(2))
    }

    @Test
    fun computesDiscountedPriceWithTwoDecimals() {
        val result = service.discountedPrice(BigDecimal("90000.00"), discount(1, "20.00"))

        assertEquals(BigDecimal("72000.00"), result)
    }

    @Test
    fun roundsDiscountedPriceHalfUp() {
        val result = service.discountedPrice(BigDecimal("10000.25"), discount(1, "50.00"))

        assertEquals(BigDecimal("5000.13"), result)
    }
}
