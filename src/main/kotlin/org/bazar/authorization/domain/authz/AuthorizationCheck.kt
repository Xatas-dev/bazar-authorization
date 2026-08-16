package org.bazar.authorization.domain.authz

import java.util.UUID

data class AuthorizationCheck(
    val spaceId: Long,
    val loggedInUserId: UUID,
    val resource: String,
    val action: String,
    val resourceId: String? = null,
    val principalAttributes: Map<String, String> = emptyMap(),
    val resourceAttributes: Map<String, String> = emptyMap()
)
