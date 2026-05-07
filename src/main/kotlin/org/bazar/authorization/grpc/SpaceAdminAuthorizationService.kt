package org.bazar.authorization.grpc

import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpcKt.AuthorizationAdminServiceCoroutineImplBase
import org.bazar.authorization.service.AuthorizationService
import org.bazar.authorization.service.RoleService
import org.bazar.authorization.service.SpaceUserService
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.toUuid
import org.bazar.authorization.utils.extensions.validate
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class SpaceAdminAuthorizationService(
    private val authorizationService: AuthorizationService,
    private val spaceUserService: SpaceUserService,
    private val roleService: RoleService
) : AuthorizationAdminServiceCoroutineImplBase() {

    /**
    Deletion logic:
    1. Find all role_id's from space_user table by specified space_id
    2. Delete all rows from space_user by specified space_id
    3. Delete all rows from roles_actions by role_id's found on step 1
    4. Delete all roles by role_id's found on step 1, ONLY USER SCOPE
     **/
    override suspend fun deleteSpace(request: DeleteSpaceRequest): DeleteSpaceResponse {
        request.validate()
        val authenticatedUserId = GrpcSecurityContext.getUserId()

        if (!authorizationService.authorize(request.spaceId, authenticatedUserId, "space", "DELETE")) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS)
        }

        val roleIds = spaceUserService.findAllAssignedRoleIdsInSpace(request.spaceId)

        suspendTransaction {
            spaceUserService.deleteAllUsersFromSpace(request.spaceId)
            roleService.deleteAllByRoleIdsAndScope(roleIds, RoleScope.USER)
        }

        return DeleteSpaceResponse.newBuilder().setSuccess(true).build()
    }

    /**
     * Create a new user in space with default role
     * Default for non creator (creator = false) is role_id = 2
     * Default for creator (creator = true) is role_id = 1
     */
    override suspend fun createUser(request: CreateUserRequest): CreateUserResponse {
        request.validate()
        val authenticatedUserId = GrpcSecurityContext.getUserId()

        if (!request.isCreator && !authorizationService.authorize(
                request.spaceId,
                authenticatedUserId,
                "space_user",
                "ADD"
            )
        ) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS)
        }

        val roleId = if (request.isCreator) 1L else 2L

        spaceUserService.saveOnConflictThrow(request.spaceId, request.userId.toUuid(), roleId)

        return CreateUserResponse.newBuilder().setSuccess(true).build()
    }

    override suspend fun deleteUser(request: DeleteUserRequest): DeleteUserResponse {
        request.validate()
        val authenticatedUserId = GrpcSecurityContext.getUserId()

        if (!authorizationService.authorize(
                request.spaceId,
                authenticatedUserId,
                "space_user",
                "DELETE",
                resourceAttributes = mapOf("creator" to request.isCreator.toString())
            )
        ) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS)
        }

        val targetUserId = request.userId.toUuid()
        val roleIdToDelete = spaceUserService.getOrNull(request.spaceId, targetUserId)?.roleId

        if (roleIdToDelete != null) {
            suspendTransaction {
                spaceUserService.deleteSpaceUser(request.spaceId, targetUserId)
                roleService.deleteAllByRoleIdsAndScope(listOf(roleIdToDelete), RoleScope.USER)
            }
        }

        return DeleteUserResponse.newBuilder().setSuccess(true).build()
    }
}