package org.bazar.authorization.utils.extensions.builder

import org.bazar.authorization.grpc.AuthorizeRequest
import org.bazar.authorization.model.commands.AuthorizeCommand
import java.util.*

fun buildAuthorizationCommand(
    spaceId: Long,
    authenticatedUserId: UUID,
    resource: String,
    action: String,
    resourceId: String? = null,
    principalAttributes: Map<String, String> = emptyMap(),
    resourceAttributes: Map<String, String> = emptyMap(),
) =
    AuthorizeCommand(
        spaceId,
        authenticatedUserId,
        resource,
        resourceId,
        action,
        principalAttributes,
        resourceAttributes
    )

fun buildAuthorizationCommand(
    request: AuthorizeRequest,
    authenticatedUserId: UUID,
) =
    AuthorizeCommand(
        request.spaceId,
        authenticatedUserId,
        request.resource,
        request.resourceId,
        request.action,
        request.principalAttributesList.associate { it.name to it.value },
        request.resourceAttributesList.associate { it.name to it.value }
    )