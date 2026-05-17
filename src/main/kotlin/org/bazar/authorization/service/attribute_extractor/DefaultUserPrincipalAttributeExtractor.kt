package org.bazar.authorization.service.attribute_extractor

import org.bazar.authorization.service.ActionService
import org.bazar.authorization.service.RoleService

class DefaultUserPrincipalAttributeExtractor(
    private val roleService: RoleService,
    private val actionService: ActionService
) : AttributeExtractor {

    /**
    Extract default attributes from authorization service:
    - is_creator from space_user table
    - requester userId
    - allowed_actions, come from actions in user role
    - actions specific attributes stored in user role_mappings
     */
    override suspend fun extract(context: AttributeExtractionContext): AttributeExtractionContext {
        val userId = context.authenticatedUser.userId.toString()
        val roleId = context.authenticatedUser.roleId

        val roleMappings = roleService.getRoleActionMappings(roleId)
        val role = roleService.getRoleById(roleId)
        val grantedActions = actionService.findAllByIds(roleMappings.map { it.actionId })

        val allowedActionKeys = grantedActions.map { "${it.resource}:${it.code}" }

        // 3. Ищем атрибуты (grantable_actions и т.д.), специфичные для ТЕКУЩЕГО запроса
        val currentActionId = grantedActions
            .find { it.resource == context.resource && it.code == context.action }
            ?.id

        val currentActionAttributes = currentActionId?.let {
            roleMappings
                .find { it.actionId == currentActionId }
                ?.assignedAttributes
        } ?: emptyMap()

        context.principalAttributes.apply {
            put("user_id", userId)
            put("role_id", roleId.toString())
            put("allowed_actions", allowedActionKeys.toString())
            put("is_creator", context.authenticatedUser.isCreator.toString())
            put("scope", role.scope.name)
            putAll(currentActionAttributes) // Добавляем grantable_actions / manageable_roles
        }

        return context
    }

    override fun isApplicable(context: AttributeExtractionContext): Boolean {
        return true
    }
}