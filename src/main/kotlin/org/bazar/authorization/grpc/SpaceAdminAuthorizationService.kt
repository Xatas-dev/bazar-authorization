package org.bazar.authorization.grpc

import io.grpc.Status
import io.grpc.StatusException
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpcKt.AuthorizationAdminServiceCoroutineImplBase
import org.bazar.authorization.model.authz.enums.AuthorizationAction.*
import org.bazar.authorization.database.entity.enums.Role
import org.bazar.authorization.service.CerbosAccessService
import org.bazar.authorization.service.UserSpaceRoleService
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.toUuid
import org.bazar.authorization.utils.extensions.validate
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class SpaceAdminAuthorizationService(
    private val userSpaceRoleService: UserSpaceRoleService,
    private val cerbosAccessService: CerbosAccessService
) : AuthorizationAdminServiceCoroutineImplBase() {

    override suspend fun deleteSpace(request: DeleteSpaceRequest): DeleteSpaceResponse {
        request.validate()
        val authenticatedUserId = GrpcSecurityContext.getUserId()

        if (!cerbosAccessService.checkAccess(authenticatedUserId, request.spaceId, DELETE_SPACE.actionName)) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS)
        }

        suspendTransaction {
            userSpaceRoleService.deleteAllFromSpace(spaceId = request.spaceId)
        }

        return DeleteSpaceResponse.newBuilder().setSuccess(true).build()
    }

    override suspend fun createSpace(request: CreateSpaceRequest): CreateSpaceResponse {
        request.validate()
        val authenticatedUserId = GrpcSecurityContext.getUserId()

        suspendTransaction {
            userSpaceRoleService.createSpaceOwner(
                userId = authenticatedUserId,
                spaceId = request.spaceId
            )
        }

        return CreateSpaceResponse.newBuilder().setSuccess(true).build()
    }

    override suspend fun addUserToSpace(request: AddUserToSpaceRequest): AddUserToSpaceResponse {
        request.validate()
        val authenticatedUserId = GrpcSecurityContext.getUserId()

        if (!cerbosAccessService.checkAccess(authenticatedUserId, request.spaceId, ADD_USER_TO_SPACE.actionName)) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS)
        }

        suspendTransaction {
            userSpaceRoleService.saveOrUpdateRoleInSpace(
                userId = request.userId.toUuid(),
                spaceId = request.spaceId,
                role = Role.valueOf(request.role)
            )
        }

        return AddUserToSpaceResponse.newBuilder().setSuccess(true).build()
    }

    override suspend fun removeUserFromSpace(request: RemoveUserFromSpaceRequest): RemoveUserFromSpaceResponse {
        request.validate()
        val authenticatedUserId = GrpcSecurityContext.getUserId()

        if (authenticatedUserId.toString() == request.userId) {
            throw ApiException(ApiExceptions.CANNOT_REMOVE_HIMSELF_FROM_SPACE)
        }

        if (!cerbosAccessService.checkAccess(authenticatedUserId, request.spaceId, REMOVE_USER_FROM_SPACE.actionName)) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS)
        }

        suspendTransaction {
            userSpaceRoleService.removeUserFromSpace(
                userId = request.userId.toUuid(),
                spaceId = request.spaceId
            )
        }

        return RemoveUserFromSpaceResponse.newBuilder().setSuccess(true).build()
    }
}