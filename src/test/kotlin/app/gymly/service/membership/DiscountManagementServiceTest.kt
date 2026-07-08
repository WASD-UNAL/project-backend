package app.gymly.service.membership

import app.gymly.dto.membership.DiscountRequest
import app.gymly.dto.membership.UpdateDiscountRequest
import app.gymly.exception.PlanInactiveException
import app.gymly.exception.PlanNotFoundException
import app.gymly.model.Discount
import app.gymly.model.Plan
import app.gymly.repository.DiscountRepository
import app.gymly.repository.PlanRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class DiscountManagementServiceTest {
    private lateinit var discountRepository: DiscountRepository
    private lateinit var planRepository: PlanRepository
    private lateinit var service: DiscountManagementService

    private val monthlyPlan = Plan(id = 1, name = "Mensual", durationDays = 30, price = BigDecimal("90000.00"))
    private val yearlyPlan = Plan(id = 2, name = "Anual", durationDays = 365, price = BigDecimal("900000.00"))
    private val inactivePlan = Plan(id = 3, name = "Retirado", durationDays = 30, price = BigDecimal("50000.00"), active = false)

    private fun request(planIds: List<Int> = emptyList()) =
        DiscountRequest(
            name = "Verano Fit",
            description = "Descuento de temporada",
            percentage = BigDecimal("20.00"),
            initDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 8, 31),
            active = true,
            planIds = planIds,
        )

    @BeforeEach
    fun setUp() {
        discountRepository = mock()
        planRepository = mock()
        service = DiscountManagementService(discountRepository, planRepository)
        whenever(discountRepository.save(any<Discount>())).thenAnswer { it.arguments[0] }
    }

    @Nested
    inner class CreateDiscount {
        @Test
        fun savesNameDescriptionAndSelectedPlans() {
            whenever(planRepository.findAllById(listOf(1, 2))).thenReturn(listOf(monthlyPlan, yearlyPlan))

            val response = service.createDiscount(request(planIds = listOf(1, 2)))

            val captor = argumentCaptor<Discount>()
            verify(discountRepository).save(captor.capture())
            assertEquals("Verano Fit", captor.firstValue.name)
            assertEquals("Descuento de temporada", captor.firstValue.description)
            assertEquals(
                setOf(1, 2),
                captor.firstValue.plans
                    .map { it.id }
                    .toSet(),
            )
            assertEquals(listOf(1, 2), response.plans.map { it.id })
        }

        @Test
        fun allowsDiscountWithoutPlans() {
            whenever(planRepository.findAllById(emptyList())).thenReturn(emptyList())

            val response = service.createDiscount(request())

            assertEquals(emptyList<Int>(), response.plans.map { it.id })
        }

        @Test
        fun rejectsUnknownPlan() {
            whenever(planRepository.findAllById(listOf(1, 99))).thenReturn(listOf(monthlyPlan))

            assertThrows(PlanNotFoundException::class.java) {
                service.createDiscount(request(planIds = listOf(1, 99)))
            }
        }

        @Test
        fun rejectsInactivePlan() {
            whenever(planRepository.findAllById(listOf(3))).thenReturn(listOf(inactivePlan))

            assertThrows(PlanInactiveException::class.java) {
                service.createDiscount(request(planIds = listOf(3)))
            }
        }
    }

    @Nested
    inner class UpdateDiscount {
        private fun existingDiscount() =
            Discount(
                id = 7,
                name = "Verano Fit",
                description = "Descuento de temporada",
                percentage = BigDecimal("20.00"),
                initDate = LocalDate.of(2026, 7, 1),
                endDate = LocalDate.of(2026, 8, 31),
                active = true,
                plans = mutableSetOf(monthlyPlan),
            )

        @Test
        fun replacesSelectedPlans() {
            whenever(discountRepository.findById(7)).thenReturn(Optional.of(existingDiscount()))
            whenever(planRepository.findAllById(listOf(2))).thenReturn(listOf(yearlyPlan))

            val response = service.updateDiscount(7, UpdateDiscountRequest(planIds = listOf(2)))

            assertEquals(listOf(2), response?.plans?.map { it.id })
        }

        @Test
        fun keepsPlansWhenPlanIdsOmitted() {
            whenever(discountRepository.findById(7)).thenReturn(Optional.of(existingDiscount()))

            val response = service.updateDiscount(7, UpdateDiscountRequest(name = "Invierno Fuerte"))

            assertEquals("Invierno Fuerte", response?.name)
            assertEquals(listOf(1), response?.plans?.map { it.id })
        }

        @Test
        fun returnsNullWhenDiscountMissing() {
            whenever(discountRepository.findById(7)).thenReturn(Optional.empty())

            assertNull(service.updateDiscount(7, UpdateDiscountRequest(name = "X")))
        }
    }
}
