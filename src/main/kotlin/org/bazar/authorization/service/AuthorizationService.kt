package org.bazar.authorization.service

import org.bazar.authorization.utils.authorization.enums.Permission
import org.bazar.authorization.utils.buildAuthorizationRequest
import java.util.*

class AuthorizationService(
    private val roleService: RoleService,
    private val spaceUserService: SpaceUserService,
    private val actionService: ActionService,
    private val cerbosAccessService: CerbosAccessService
) {

    /**
     * Main authorization method.
     * To check authorization :
     * 1. Check that specified action exist in DB (throw 404 if not)
     * 2. Find roleId from space_user by specified spaceId and userId (throw 401 if no such)
     * 3. Find role-action mapping from roles_actions with specified role_id and action_id (throw 401 if no such)
     * 4. Check authorization decision via cerbos
     */
    suspend fun authorize(
        spaceId: Long,
        userId: UUID,
        resource: String,
        action: String,
        customAttributes: Map<String, String>? = null
    ): Boolean {
        val existingAction = actionService.getActionByNameAndResourceOrThrow(action, resource)
        val spaceUserInDb = spaceUserService.getSpaceUser(spaceId, userId)
        val attributes =
            roleService.getRoleActionMappings(spaceUserInDb.roleId, existingAction.id)
                .assignedAttributes?.plus(customAttributes ?: emptyMap()) ?: emptyMap()
        val authzRequest = buildAuthorizationRequest(spaceUserInDb, resource, action, attributes)

        return cerbosAccessService.checkAccess(authzRequest)
    }

    /*
        Authorize with predefined Permission (resource + action). Used for admin API.
     */
    suspend fun authorize(
        spaceId: Long,
        userId: UUID,
        permission: Permission,
        customAttributes: Map<String, String>? = null
    ): Boolean {
        return authorize(spaceId, userId, permission.resource, permission.action, customAttributes)
    }

}