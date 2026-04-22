package org.bazar.authorization.model.authz

import java.util.*

data class AuthorizationRequest(
    val spaceId: Long,
    val userId: UUID,
    val creator: Boolean,
    val resource: String,
    val action: String,
    val attributes: Map<String, String>?
)
