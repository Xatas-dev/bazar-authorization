package org.bazar.authorization.model.authz

import java.util.*

data class AuthorizationRequest(
    val spaceId: Long,
    val userId: UUID,
    val resource: String,
    val action: String,
    val principalAttributes: Map<String, String>,
    val resourceAttributes: Map<String, String>
)
