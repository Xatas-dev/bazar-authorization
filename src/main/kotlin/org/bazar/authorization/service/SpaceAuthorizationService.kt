package org.bazar.authorization.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.bazar.authorization.config.grpc.interceptors.KotlinSecurityContextHolder
import org.bazar.authorization.grpc.*
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpcKt.AuthorizationAdminServiceCoroutineImplBase
import org.bazar.authorization.grpc.AuthorizationServiceGrpcKt.AuthorizationServiceCoroutineImplBase
import org.bazar.authorization.model.authz.enums.AuthorizationAction.ADD_USER_TO_SPACE
import org.bazar.authorization.model.authz.enums.AuthorizationAction.REMOVE_USER_FROM_SPACE
import org.bazar.authorization.persistence.entity.enums.Role
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.toAuthorizationAction
import org.bazar.authorization.utils.extensions.toUuid
import org.bazar.authorization.utils.extensions.validate
import org.springframework.grpc.server.service.GrpcService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.util.UUID
import kotlin.coroutines.CoroutineContext

@GrpcService
class SpaceAuthorizationService(
    private val userSpaceRoleService: UserSpaceRoleService,
    private val cerbosAccessService: CerbosAccessService
) : AuthorizationServiceCoroutineImplBase() {

    private val logger = KotlinLogging.logger { }

    override suspend fun authorize(request: AuthorizeRequest): AuthorizeResponse = request.let {
        it.validate()
        var allowed = false
        if (it.kind == "space") {
            val authenticatedUserId = getUserIdFromSecurityContext()
            try {
                userSpaceRoleService.getUserRole(authenticatedUserId, it.resourceId)
                allowed = cerbosAccessService.checkAccess(authenticatedUserId, it.resourceId, it.action.toAuthorizationAction())
            }
            catch (ex: ApiException){
                logger.warn { "Action ${it.action} for user $authenticatedUserId is not allowed due to ${ex.getFullErrorMessage()}" }
                allowed = false
            }
        }
        AuthorizeResponse.newBuilder().setAllowed(allowed).build()
    }

    private fun getUserIdFromSecurityContext(): UUID {
        val authentication = KotlinSecurityContextHolder.getContext().authentication
            ?: throw ApiException(ApiExceptions.NOT_AUTHENTICATED)
        val jwt = authentication.principal as Jwt
        return jwt.subject.toUuid()
    }
}