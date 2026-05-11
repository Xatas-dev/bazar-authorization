package org.bazar.authorization.model.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoleRequest(
    val spaceId: Long,
    val name: String,
    val isVisible: Boolean,
    val actions: List<CreateRoleRequestActionDto>
)


@Serializable
data class CreateRoleRequestActionDto (
    val id: Int,
    val attributes: List<CreateRoleRequestAttributeDto> = emptyList()
)

@Serializable
data class CreateRoleRequestAttributeDto (
    val id: Int,
    val value: String
)