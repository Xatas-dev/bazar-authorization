package org.bazar.authorization.grpc

import org.bazar.authorization.grpc.AuthorizationServiceGrpcKt.AuthorizationServiceCoroutineImplBase
import org.bazar.authorization.service.AuthorizationService
import org.bazar.authorization.utils.extensions.builder.buildAuthorizationCommand
import org.bazar.authorization.utils.extensions.validate
import org.slf4j.LoggerFactory

class SpaceAuthorizationService(
    private val authorizationService: AuthorizationService
) : AuthorizationServiceCoroutineImplBase() {

    val logger = LoggerFactory.getLogger(javaClass)!!

    override suspend fun authorize(request: AuthorizeRequest): AuthorizeResponse {
        request.validate()
        val authenticatedUserId = GrpcSecurityContext.getUserId()

        val authorizeCommand = buildAuthorizationCommand(request, authenticatedUserId)

        val allowed = authorizationService.authorize(authorizeCommand)

        return AuthorizeResponse.newBuilder()
            .setAllowed(allowed)
            .build()
    }
}