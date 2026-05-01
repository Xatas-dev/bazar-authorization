package org.bazar.authorization.model.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class GetRoleNamesResponse(
    val roles: List<GetRoleNameDto>
)

@Serializable
data class GetRoleNameDto(
    val name: String,
    val userId: String
)