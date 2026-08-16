package org.bazar.authorization.adapter.inbound.rest.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetActionWithAssignedAttributesDto(
    val id: Int,
    val code: String,
    val name: String,
    val resource: String,
    val resourceName: String,
    val attributes: List<GetAssignedActionAttributeDto>
)
