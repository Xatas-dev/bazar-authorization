package org.bazar.authorization.service

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
    suspend fun authorize(spaceId: Long, userId: UUID, resource: String, action: String): Boolean {
        val existingAction = actionService.getActionByNameAndResourceOrThrow(action, resource)
        val spaceUserInDb = spaceUserService.getOrThrow(spaceId, userId)
        val roleWithActionAndAttributes =
            roleService.getRoleWithActionAndAttributesOrThrow(spaceUserInDb.roleId, existingAction.id)
        val authzRequest = buildAuthorizationRequest(roleWithActionAndAttributes, spaceUserInDb, resource, action)

        return cerbosAccessService.checkAccess(authzRequest)
    }

}