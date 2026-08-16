package org.bazar.authorization.application.shared.impl

import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.shared.AttributeExtractor
import org.bazar.authorization.application.shared.AttributeExtractionContext
import org.bazar.authorization.domain.authz.PrincipalAttributes
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException

class DefaultUserPrincipalAttributeExtractor(
    private val rolesActionsRepositoryPort: RolesActionsRepositoryPort,
    private val roleRepositoryPort: RoleRepositoryPort,
    private val actionRepositoryPort: ActionRepositoryPort
) : AttributeExtractor {

    override fun extract(context: AttributeExtractionContext): AttributeExtractionContext {
        val userId = context.authenticatedUser.userId.toString()
        val roleId = context.authenticatedUser.roleId

        val roleMappings = rolesActionsRepositoryPort.findAllByRoleId(roleId)
        val role = roleRepositoryPort.findById(roleId)
            ?: throw DomainException(DomainErrors.NO_SUCH_ROLE, "roleId: $roleId")
        val grantedActions = actionRepositoryPort.findByIds(roleMappings.map { it.actionId })

        val allowedActionKeys = grantedActions.map { "${it.resource}:${it.code}" }

        val currentActionId = grantedActions
            .find { it.resource == context.resource && it.code == context.action }
            ?.id

        val currentActionAttributes = currentActionId?.let {
            roleMappings
                .find { it.actionId == currentActionId }
                ?.assignedAttributes
        } ?: emptyMap()

        context.principalAttributes.apply {
            put(PrincipalAttributes.USER_ID, userId)
            put(PrincipalAttributes.ROLE_ID, roleId.toString())
            put(PrincipalAttributes.ALLOWED_ACTIONS, allowedActionKeys.toString())
            put(PrincipalAttributes.IS_CREATOR, context.authenticatedUser.isCreator.toString())
            put(PrincipalAttributes.SCOPE, role.scope.name)
            putAll(currentActionAttributes)
        }

        return context
    }

    override fun isApplicable(context: AttributeExtractionContext): Boolean = true
}
