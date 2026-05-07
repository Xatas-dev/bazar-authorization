package org.bazar.authorization.service

import org.bazar.authorization.grpc.AuthorizeRequest
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
        request: AuthorizeRequest,
        loggedInUserId: UUID
    ): Boolean {
        return authorize(
            request.spaceId,
            loggedInUserId,
            request.resource,
            request.action,
            request.principalAttributesList.associate { it.name to it.value },
            request.resourceAttributesList.associate { it.name to it.value })
    }

    /*
        Authorize with predefined Permission (resource + action). Used for admin API.
     */
    suspend fun authorize(
        spaceId: Long,
        loggedInUserId: UUID,
        resource: String,
        action: String,
        principalAttributes: Map<String, String> = emptyMap(),
        resourceAttributes: Map<String, String> = emptyMap()
    ): Boolean {

        val existingAction = actionService.getActionByNameAndResourceOrThrow(action, resource)
        val spaceUserInDb = spaceUserService.getSpaceUser(spaceId, loggedInUserId)

        val enrichedPrincipalAttributes =
            principalAttributes + (roleService.getRoleActionMappings(spaceUserInDb.roleId, existingAction.id)
                .assignedAttributes ?: emptyMap())


        val authzRequest =
            buildAuthorizationRequest(spaceUserInDb, resource, action, enrichedPrincipalAttributes, resourceAttributes)

        return cerbosAccessService.checkAccess(authzRequest)

    }

    suspend fun checkIfUserInSpace(userId: UUID, spaceId: Long) {
        spaceUserService.getSpaceUser(spaceId, userId)
    }

}