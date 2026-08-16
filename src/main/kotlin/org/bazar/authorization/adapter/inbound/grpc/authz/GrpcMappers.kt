package org.bazar.authorization.adapter.inbound.grpc.authz

import org.bazar.authorization.domain.authz.AuthorizationCheck
import org.bazar.authorization.grpc.AuthorizeRequest
import java.util.UUID

fun AuthorizeRequest.toAuthorizationCheck(authenticatedUserId: UUID) = AuthorizationCheck(
    spaceId = spaceId,
    loggedInUserId = authenticatedUserId,
    resource = resource,
    action = action,
    resourceId = resourceId.takeIf { it.isNotEmpty() },
    principalAttributes = principalAttributesList.associate { it.name to it.value },
    resourceAttributes = resourceAttributesList.associate { it.name to it.value }
)
