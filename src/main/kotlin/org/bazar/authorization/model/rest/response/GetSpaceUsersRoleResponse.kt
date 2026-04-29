package org.bazar.authorization.model.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class GetSpaceUsersRoleResponse(
    val id: Long,
    val name: String?,
    val spaceId: Long?,
    val actions: List<GetActionWithAssignedAttributesDto>
)
