package org.bazar.authorization.application.shared

import org.bazar.authorization.domain.authz.AuthorizationCheck
import org.bazar.authorization.domain.authz.Permission
import java.util.UUID

fun buildAuthorizationCheck(
    spaceId: Long,
    userId: UUID,
    permission: Permission,
    resourceId: String? = null,
    principalAttributes: Map<String, String> = emptyMap(),
    resourceAttributes: Map<String, String> = emptyMap()
): AuthorizationCheck = AuthorizationCheck(
    spaceId = spaceId,
    loggedInUserId = userId,
    resource = permission.resource,
    action = permission.action,
    resourceId = resourceId,
    principalAttributes = principalAttributes,
    resourceAttributes = resourceAttributes
)
