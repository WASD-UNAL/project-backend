package app.gymly.service.presentation

import app.gymly.model.Discount
import app.gymly.model.Plan
import app.gymly.repository.PlanRepository
import app.gymly.service.membership.DiscountPricingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class PlanViewServiceTest {
    private lateinit var planRepository: PlanRepository
    private lateinit var discountPricingService: DiscountPricingService
    private lateinit var service: PlanViewService

    private val monthlyPlan = Plan(id = 1, name = "Mensual", durationDays = 30, price = BigDecimal("90000.00"))
    private val yearlyPlan = Plan(id = 2, name = "Anual", durationDays = 365, price = BigDecimal("900000.00"))

    private val summerDiscount =
        Discount(
            id = 5,
            name = "Verano Fit",
            description = "Temporada de verano",
            percentage = BigDecimal("20.00"),
            initDate = LocalDate.now().minusDays(1),
            endDate = LocalDate.now().plusDays(30),
        )

    @BeforeEach
    fun setUp() {
        planRepository = mock()
        discountPricingService = mock()
        service = PlanViewService(planRepository, discountPricingService)
        whenever(discountPricingService.discountedPrice(BigDecimal("90000.00"), summerDiscount))
            .thenReturn(BigDecimal("72000.00"))
    }

    @Test
    fun activePlansIncludeCurrentDiscountWithDiscountedPrice() {
        whenever(planRepository.findByActiveTrue()).thenReturn(listOf(monthlyPlan, yearlyPlan))
        whenever(discountPricingService.currentDiscountsByPlan()).thenReturn(mapOf(1 to summerDiscount))

        val plans = service.getActivePlans()

        val discounted = plans.first { it.id == 1 }.discount
        assertEquals("Verano Fit", discounted?.name)
        assertEquals(BigDecimal("20.00"), discounted?.percentage)
        assertEquals(BigDecimal("72000.00"), discounted?.discountedPrice)
        assertNull(plans.first { it.id == 2 }.discount)
    }

    @Test
    fun planByIdIncludesCurrentDiscount() {
        whenever(planRepository.findById(1)).thenReturn(Optional.of(monthlyPlan))
        whenever(discountPricingService.currentDiscountFor(1)).thenReturn(summerDiscount)

        val plan = service.getPlanById(1)

        assertEquals(BigDecimal("72000.00"), plan?.discount?.discountedPrice)
    }
}
