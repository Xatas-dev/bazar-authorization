package org.bazar.authorization.model.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class GetRolesResponse(
    val id: Long,
    val name: String?,
    val isVisible: Boolean,
    val createdBy: String,
    val spaceId: Long?,
    val actions: List<GetActionWithAssignedAttributesDto>
)
