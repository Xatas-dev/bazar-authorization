package org.bazar.authorization.adapter.inbound.rest.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class PutRoleRequest(
    val name: String,
    val isVisible: Boolean,
    val actions: List<SimpleActionDto>
)