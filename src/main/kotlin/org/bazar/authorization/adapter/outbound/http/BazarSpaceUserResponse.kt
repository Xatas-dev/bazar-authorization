package org.bazar.authorization.adapter.outbound.http

import kotlinx.serialization.Serializable

@Serializable
data class BazarSpaceUserResponse(
    val userId: String,
    val spaceId: Long,
    val creator: Boolean
)
