package org.bazar.authorization.adapter.inbound.rest.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetAssignedActionAttributeDto(
    val id: Int,
    val name: String,
    val displayName: String,
    val valueType: String,
    val value: String?
)
