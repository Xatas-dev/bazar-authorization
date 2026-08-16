package org.bazar.authorization.adapter.inbound.rest.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetActionAttributeDto(
    val id: Int,
    val name: String,
    val displayName: String,
    val valueType: String
)
