package org.bazar.authorization.adapter.inbound.rest.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetRoleNamesResponse(
    val roles: List<GetRoleNameDto>
)

@Serializable
data class GetRoleNameDto(
    val id: Long,
    val name: String,
    val userId: String,
    val isVisible: Boolean,
    val isCreator: Boolean
)