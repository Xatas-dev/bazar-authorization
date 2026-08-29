package org.bazar.authorization.adapter.inbound.grpc.authz

import org.bazar.authorization.adapter.inbound.grpc.validate
import org.bazar.authorization.application.authz.port.`in`.AuthorizeUseCase
import org.bazar.authorization.application.shared.AuthenticatedUserProvider
import org.bazar.authorization.grpc.AuthorizationServiceGrpcKt.AuthorizationServiceCoroutineImplBase
import org.bazar.authorization.grpc.AuthorizeRequest
import org.bazar.authorization.grpc.AuthorizeResponse

class SpaceAuthorizationService(
    private val authorizeUseCase: AuthorizeUseCase,
    private val authenticatedUserProvider: AuthenticatedUserProvider
) : AuthorizationServiceCoroutineImplBase() {

    override suspend fun authorize(request: AuthorizeRequest): AuthorizeResponse {
        request.validate()
        val authenticatedUserId = authenticatedUserProvider.getUserId()

        val allowed = authorizeUseCase.execute(request.toAuthorizationCheck(authenticatedUserId))

        return AuthorizeResponse.newBuilder()
            .setAllowed(allowed)
            .build()
    }
}
