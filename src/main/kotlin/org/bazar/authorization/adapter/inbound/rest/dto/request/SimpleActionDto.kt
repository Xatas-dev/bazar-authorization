package org.bazar.authorization.adapter.inbound.rest.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SimpleActionDto(
    val id: Int,
    val attributes: List<SimpleAttributeDto> = emptyList()
)

@Serializable
data class SimpleAttributeDto(
    val id: Int,
    val value: String
)
