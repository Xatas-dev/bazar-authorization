package org.bazar.authorization.application.shared

import org.bazar.authorization.domain.authz.AuthorizationCheck
import org.bazar.authorization.domain.authz.AuthorizationAction
import java.util.UUID

fun buildAuthorizationCheck(
    spaceId: Long,
    userId: UUID,
    authorizationConstant: AuthorizationAction,
    resourceId: String? = null,
    principalAttributes: Map<String, String> = emptyMap(),
    resourceAttributes: Map<String, String> = emptyMap()
): AuthorizationCheck = AuthorizationCheck(
    spaceId = spaceId,
    loggedInUserId = userId,
    resource = authorizationConstant.resource,
    action = authorizationConstant.action,
    resourceId = resourceId,
    principalAttributes = principalAttributes,
    resourceAttributes = resourceAttributes
)
