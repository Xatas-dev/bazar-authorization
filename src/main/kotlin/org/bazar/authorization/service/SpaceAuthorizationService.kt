package org.bazar.authorization.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.stub.StreamObserver
import org.bazar.authorization.config.grpc.interceptors.KotlinSecurityContextHolder
import org.bazar.authorization.grpc.AuthorizationServiceGrpc.AuthorizationServiceImplBase
import org.bazar.authorization.grpc.AuthorizeRequest
import org.bazar.authorization.grpc.AuthorizeResponse
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.toUuid
import org.bazar.authorization.utils.extensions.validate
import org.springframework.grpc.server.service.GrpcService
import org.springframework.security.oauth2.jwt.Jwt
import java.util.*

@GrpcService
class SpaceAuthorizationService(
    private val userSpaceRoleService: UserSpaceRoleService,
    private val cerbosAccessService: CerbosAccessService
) : AuthorizationServiceImplBase() {

    private val logger = KotlinLogging.logger { }
    override fun authorize(
        request: AuthorizeRequest,
        responseObserver: StreamObserver<AuthorizeResponse>
    ) {
        request.validate()
        var allowed = false
        if (request.kind == "space") {
            val authenticatedUserId = getUserIdFromSecurityContext()
            try {
                userSpaceRoleService.getUserRole(authenticatedUserId, request.resourceId)
                allowed = cerbosAccessService.checkAccess(authenticatedUserId, request.resourceId, request.action)
            } catch (ex: ApiException) {
                logger.warn { "Action ${request.action} for user $authenticatedUserId is not allowed due to ${ex.getFullErrorMessage()}" }
                allowed = false
            }
        }
        responseObserver.onNext(
            AuthorizeResponse.newBuilder().setAllowed(allowed).build()
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