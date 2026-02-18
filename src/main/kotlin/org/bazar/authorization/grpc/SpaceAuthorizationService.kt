package org.bazar.authorization.grpc

import org.bazar.authorization.grpc.AuthorizationServiceGrpcKt.AuthorizationServiceCoroutineImplBase
import org.bazar.authorization.service.CerbosAccessService
import org.bazar.authorization.service.UserSpaceRoleService
import org.bazar.authorization.utils.extensions.validate
import org.slf4j.LoggerFactory

class SpaceAuthorizationService(
    private val userSpaceRoleService: UserSpaceRoleService,
    private val cerbosAccessService: CerbosAccessService
) : AuthorizationServiceCoroutineImplBase() {

    val logger = LoggerFactory.getLogger(javaClass)!!

    override suspend fun authorize(request: AuthorizeRequest): AuthorizeResponse {
        request.validate()
        var allowed = false

        if (request.kind == "space") {
            val authenticatedUserId = GrpcSecurityContext.getUserId()
            try {
                // Cerbos call (which calls DB inside to fetch user role)
                allowed = cerbosAccessService.checkAccess(
                    userId = authenticatedUserId,
                    spaceId = request.resourceId,
                    action = request.action
                )
            } catch (ex: Exception) {
                logger.warn("Authorization failed for user $authenticatedUserId on resource ${request.resourceId}: ${ex.message}")
                allowed = false
            }
        }

        return AuthorizeResponse.newBuilder()
            .setAllowed(allowed)
            .build()
    }
}