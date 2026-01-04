package org.bazar.authorization.service

import jakarta.transaction.Transactional
import org.bazar.authorization.config.grpc.interceptors.KotlinSecurityContextHolder
import org.bazar.authorization.grpc.*
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpcKt.AuthorizationAdminServiceCoroutineImplBase
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
import java.util.*

@GrpcService
class SpaceAdminAuthorizationService(
    private val userSpaceRoleService: UserSpaceRoleService,
    private val cerbosAccessService: CerbosAccessService
) : AuthorizationAdminServiceCoroutineImplBase() {

    override suspend fun deleteSpace(request: DeleteSpaceRequest): DeleteSpaceResponse = request.let {
        it.validate()
        val authenticatedUserId = getUserIdFromSecurityContext()
        if (!cerbosAccessService.checkAccess(authenticatedUserId, it.spaceId, AuthorizationAction.DELETE_SPACE.actionName))
            throw ApiException(INSUFFICIENT_PERMISSIONS)
        userSpaceRoleService.deleteAllFromSpace(spaceId = it.spaceId)
        DeleteSpaceResponse.newBuilder().setSuccess(true).build()
    }

    override suspend fun createSpace(request: CreateSpaceRequest): CreateSpaceResponse = request.let {
        it.validate()
        userSpaceRoleService.createSpaceOwner(
            userId = getUserIdFromSecurityContext(),
            spaceId = it.spaceId
        )
        CreateSpaceResponse.newBuilder().setSuccess(true).build()
    }

    @Transactional
    override suspend fun addUserToSpace(request: AddUserToSpaceRequest): AddUserToSpaceResponse = request.let {
        it.validate()
        val authenticatedUserId = getUserIdFromSecurityContext()
        if (!cerbosAccessService.checkAccess(authenticatedUserId, it.spaceId, ADD_USER_TO_SPACE.actionName))
            throw ApiException(INSUFFICIENT_PERMISSIONS)
        userSpaceRoleService.saveOrUpdateRoleInSpace(
            userId = it.userId.toUuid(),
            spaceId = it.spaceId,
            role = Role.valueOf(it.role)
        )

        AddUserToSpaceResponse.newBuilder().setSuccess(true).build()
    }

    @Transactional
    override suspend fun removeUserFromSpace(request: RemoveUserFromSpaceRequest): RemoveUserFromSpaceResponse =
        request.let {
            it.validate()
            val authenticatedUserId = getUserIdFromSecurityContext()
            if (!cerbosAccessService.checkAccess(authenticatedUserId, it.spaceId, REMOVE_USER_FROM_SPACE.actionName))
                throw ApiException(INSUFFICIENT_PERMISSIONS)
            userSpaceRoleService.removeUserFromSpace(
                userId = it.userId.toUuid(),
                spaceId = it.spaceId
            )
            RemoveUserFromSpaceResponse.newBuilder().setSuccess(true).build()
        }

    private fun getUserIdFromSecurityContext(): UUID {
        val authentication = KotlinSecurityContextHolder.getContext().authentication
            ?: throw ApiException(ApiExceptions.NOT_AUTHENTICATED)
        val jwt = authentication.principal as Jwt
        return jwt.subject.toUuid()
    }
}