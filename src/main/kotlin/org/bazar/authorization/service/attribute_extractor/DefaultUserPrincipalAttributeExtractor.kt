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
        val requesterRoleMappings = roleService.getRoleActionMappings(context.authenticatedUser.roleId)
        val requesterActions = actionService.findAllByIds(requesterRoleMappings.map { it.actionId })
        val toBeAuthorizedAction =
            requesterActions.first { it.resource == context.resource && it.code == context.action }
        val actionSpecificAttributes = requesterRoleMappings.first { it.actionId == toBeAuthorizedAction.id }

        context.principalAttributes.apply {
            put("user_id", context.authenticatedUser.userId.toString())
            put("is_creator", context.authenticatedUser.userId.toString())
            put("allowed_actions", requesterActions.map { it.resource + ":" + it.code }.toString())
            actionSpecificAttributes.assignedAttributes?.let {
                putAll(it)
            }
        }

        return context
    }

    override fun isApplicable(context: AttributeExtractionContext): Boolean {
        return true
    }
}