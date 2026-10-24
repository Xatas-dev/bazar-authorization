package org.bazar.authorization.adapter.inbound.rest.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SimpleActionDto(
    val id: Int
)