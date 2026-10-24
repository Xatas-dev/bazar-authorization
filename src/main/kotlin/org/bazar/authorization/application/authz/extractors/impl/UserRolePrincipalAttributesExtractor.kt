package org.bazar.authorization.application.authz.extractors.impl

import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.authz.extractors.AttributeExtractor
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.shared.AttributeExtractionContext
import org.bazar.authorization.domain.authz.PrincipalAttributes
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException

class UserRolePrincipalAttributesExtractor(
    private val roleRepositoryPort: RoleRepositoryPort,
    private val actionRepositoryPort: ActionRepositoryPort,
    private val rolesActionsRepositoryPort: RolesActionsRepositoryPort
): AttributeExtractor {
    override fun extract(context: AttributeExtractionContext): AttributeExtractionContext {
        val user = context.authenticatedUser
        val role = roleRepositoryPort.findById(context.authenticatedUser.roleId)
            ?: throw DomainException(
                DomainErrors.NO_SUCH_ROLE,
                "roleId: ${context.authenticatedUser.roleId}"
            )
        val roleMappings = rolesActionsRepositoryPort.findAllByRoleId(user.roleId)
        val grantedActions = actionRepositoryPort.findByIds(roleMappings.map { it.actionId })

        val allowedActionKeys = grantedActions.map { "${it.resource}:${it.code}" }

        return context.withPrincipalAttributes(
            PrincipalAttributes.SCOPE to role.scope.name,
            PrincipalAttributes.ROLE_ID to user.roleId.toString(),
            PrincipalAttributes.ALLOWED_ACTIONS to allowedActionKeys.toString()
        )
    }

    override fun isApplicable(context: AttributeExtractionContext): Boolean = true
}