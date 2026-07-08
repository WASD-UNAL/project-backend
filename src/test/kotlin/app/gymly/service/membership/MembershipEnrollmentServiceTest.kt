package app.gymly.service.membership

import app.gymly.dto.membership.EnrollRequest
import app.gymly.dto.membership.MyMembershipResponse
import app.gymly.model.Discount
import app.gymly.model.Membership
import app.gymly.model.Payment
import app.gymly.model.PaymentMethod
import app.gymly.model.Plan
import app.gymly.repository.MembershipRepository
import app.gymly.repository.PaymentRepository
import app.gymly.repository.PlanRepository
import app.gymly.service.presentation.MembershipViewService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class MembershipEnrollmentServiceTest {
    private lateinit var membershipRepository: MembershipRepository
    private lateinit var planRepository: PlanRepository
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var membershipViewService: MembershipViewService
    private lateinit var discountPricingService: DiscountPricingService
    private lateinit var service: MembershipEnrollmentService

    private val plan = Plan(id = 10, name = "Mensual", durationDays = 30, price = BigDecimal("90000.00"))

    private val summerDiscount =
        Discount(
            id = 5,
            name = "Verano Fit",
            percentage = BigDecimal("20.00"),
            initDate = LocalDate.now().minusDays(1),
            endDate = LocalDate.now().plusDays(30),
        )

    private val membershipView =
        MyMembershipResponse(
            hasActiveMembership = false,
            pendingApproval = true,
            membershipId = 1,
            planId = 10,
            planName = "Mensual",
            price = BigDecimal("90000.00"),
            initDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(30),
            statusColor = "YELLOW",
            daysRemaining = 30,
            message = "Pendiente",
        )

    @BeforeEach
    fun setUp() {
        membershipRepository = mock()
        planRepository = mock()
        paymentRepository = mock()
        membershipViewService = mock()
        discountPricingService = mock()
        service =
            MembershipEnrollmentService(
                membershipRepository,
                planRepository,
                paymentRepository,
                membershipViewService,
                discountPricingService,
            )

        whenever(planRepository.findByIdOrNull(10)).thenReturn(plan)
        whenever(membershipRepository.findFirstByUserIdOrderByIdDesc(1)).thenReturn(null)
        whenever(membershipRepository.save(any<Membership>())).thenAnswer { invocation ->
            val membership = invocation.arguments[0] as Membership
            Membership(
                id = 1,
                userId = membership.userId,
                planId = membership.planId,
                initDate = membership.initDate,
                endDate = membership.endDate,
                status = membership.status,
            )
        }
        whenever(paymentRepository.save(any<Payment>())).thenAnswer { it.arguments[0] }
        whenever(membershipViewService.getMyMembership(1)).thenReturn(membershipView)
    }

    @Test
    fun cashEnrollmentUsesDiscountedAmountAndLinksDiscount() {
        whenever(discountPricingService.currentDiscountFor(10)).thenReturn(summerDiscount)
        whenever(discountPricingService.discountedPrice(BigDecimal("90000.00"), summerDiscount))
            .thenReturn(BigDecimal("72000.00"))

        service.enroll(1, EnrollRequest(planId = 10, paymentMethod = PaymentMethod.CASH))

        val captor = argumentCaptor<Payment>()
        verify(paymentRepository).save(captor.capture())
        assertEquals(BigDecimal("72000.00"), captor.firstValue.amount)
        assertEquals(5, captor.firstValue.discountId)
    }

    @Test
    fun cashEnrollmentWithoutDiscountUsesPlanPrice() {
        whenever(discountPricingService.currentDiscountFor(10)).thenReturn(null)

        service.enroll(1, EnrollRequest(planId = 10, paymentMethod = PaymentMethod.CASH))

        val captor = argumentCaptor<Payment>()
        verify(paymentRepository).save(captor.capture())
        assertEquals(BigDecimal("90000.00"), captor.firstValue.amount)
        assertNull(captor.firstValue.discountId)
    }
}
