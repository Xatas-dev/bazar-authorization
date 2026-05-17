package org.bazar.authorization.model.rest.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoleRequest(
    val spaceId: Long,
    val name: String,
    val isVisible: Boolean,
    val actions: List<SimpleActionDto>
)