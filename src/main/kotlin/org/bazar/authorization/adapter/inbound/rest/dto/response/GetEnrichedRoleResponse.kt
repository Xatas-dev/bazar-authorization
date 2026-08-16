package org.bazar.authorization.adapter.inbound.rest.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetEnrichedRoleResponse(
    val id: Long,
    val name: String?,
    val isVisible: Boolean,
    val createdBy: String,
    val spaceId: Long?,
    val actions: List<GetActionWithAssignedAttributesDto>
)
