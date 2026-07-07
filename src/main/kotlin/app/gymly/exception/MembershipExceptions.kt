package app.gymly.exception

class PlanNotFoundException(
    planId: Int,
) : RuntimeException("Plan '$planId' not found")

class PlanInactiveException(
    planId: Int,
) : RuntimeException("Plan '$planId' is not active")

class MembershipAlreadyActiveException : RuntimeException("User already has an active membership")

class MembershipPendingApprovalException : RuntimeException("User already has a membership pending payment approval")

class NoActiveMembershipException : RuntimeException("User has no active membership")
