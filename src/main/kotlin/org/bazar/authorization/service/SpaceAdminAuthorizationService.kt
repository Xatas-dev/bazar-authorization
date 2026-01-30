package org.bazar.authorization.service

import io.grpc.stub.StreamObserver
import org.bazar.authorization.config.grpc.interceptors.KotlinSecurityContextHolder
import org.bazar.authorization.grpc.*
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc.AuthorizationAdminServiceImplBase
import org.bazar.authorization.model.authz.enums.AuthorizationAction
import org.bazar.authorization.model.authz.enums.AuthorizationAction.ADD_USER_TO_SPACE
import org.bazar.authorization.model.authz.enums.AuthorizationAction.REMOVE_USER_FROM_SPACE
import org.bazar.authorization.persistence.entity.enums.Role
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.exceptions.ApiExceptions.INSUFFICIENT_PERMISSIONS
import org.bazar.authorization.utils.extensions.toUuid
import org.bazar.authorization.utils.extensions.validate
import org.springframework.grpc.server.service.GrpcService
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.transaction.annotation.Transactional
import java.util.*

@GrpcService
@Transactional
class SpaceAdminAuthorizationService(
    private val userSpaceRoleService: UserSpaceRoleService,
    private val cerbosAccessService: CerbosAccessService
) : AuthorizationAdminServiceImplBase() {

    override fun deleteSpace(
        request: DeleteSpaceRequest,
        responseObserver: StreamObserver<DeleteSpaceResponse>
    ) {
        request.validate()
        val authenticatedUserId = getUserIdFromSecurityContext()
        if (!cerbosAccessService.checkAccess(
                authenticatedUserId,
                request.spaceId,
                AuthorizationAction.DELETE_SPACE.actionName
            )
        )
            throw ApiException(INSUFFICIENT_PERMISSIONS)
        userSpaceRoleService.deleteAllFromSpace(spaceId = request.spaceId)

        responseObserver.onNext(
            DeleteSpaceResponse.newBuilder().setSuccess(true).build()
        )
    }

    override fun createSpace(
        request: CreateSpaceRequest,
        responseObserver: StreamObserver<CreateSpaceResponse>
    ) {
        request.validate()
        userSpaceRoleService.createSpaceOwner(
            userId = getUserIdFromSecurityContext(),
            spaceId = request.spaceId
        )
        responseObserver.onNext(
            CreateSpaceResponse.newBuilder().setSuccess(true).build()
        )
        responseObserver.onCompleted()
    }

    override fun addUserToSpace(
        request: AddUserToSpaceRequest,
        responseObserver: StreamObserver<AddUserToSpaceResponse>
    ) {
        request.validate()
        val authenticatedUserId = getUserIdFromSecurityContext()
        if (!cerbosAccessService.checkAccess(authenticatedUserId, request.spaceId, ADD_USER_TO_SPACE.actionName))
            throw ApiException(INSUFFICIENT_PERMISSIONS)
        userSpaceRoleService.saveOrUpdateRoleInSpace(
            userId = request.userId.toUuid(),
            spaceId = request.spaceId,
            role = Role.valueOf(request.role)
        )

        responseObserver.onNext(
            AddUserToSpaceResponse.newBuilder().setSuccess(true).build()
        )

        responseObserver.onCompleted()
    }

    override fun removeUserFromSpace(
        request: RemoveUserFromSpaceRequest,
        responseObserver: StreamObserver<RemoveUserFromSpaceResponse>
    ) {
        request.validate()
        val authenticatedUserId = getUserIdFromSecurityContext()
        if (authenticatedUserId.toString() == request.userId)
            throw ApiException(ApiExceptions.ILLEGAL_ARGUMENT)
        if (!cerbosAccessService.checkAccess(authenticatedUserId, request.spaceId, REMOVE_USER_FROM_SPACE.actionName))
            throw ApiException(INSUFFICIENT_PERMISSIONS)
        userSpaceRoleService.removeUserFromSpace(
            userId = request.userId.toUuid(),
            spaceId = request.spaceId
        )
        responseObserver.onNext(
            RemoveUserFromSpaceResponse.newBuilder().setSuccess(true).build()
        )

        responseObserver.onCompleted()
    }

    private fun getUserIdFromSecurityContext(): UUID {
        val authentication = KotlinSecurityContextHolder.getContext().authentication
            ?: throw ApiException(ApiExceptions.NOT_AUTHENTICATED)
        val jwt = authentication.principal as Jwt
        return jwt.subject.toUuid()
    }
}