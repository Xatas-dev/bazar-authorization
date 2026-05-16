package org.bazar.authorization.model.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class GetRolesResponse(
    val roles: List<GetRoleDto> = emptyList()
)


@Serializable
data class GetRoleDto(
    val id: Long,
    val name: String,
    val spaceId: Long,
    val scope: String,
    val isVisible: Boolean,
    val createdBy: String?
)