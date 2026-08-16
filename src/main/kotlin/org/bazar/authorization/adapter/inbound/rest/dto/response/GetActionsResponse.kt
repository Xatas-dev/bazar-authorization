package org.bazar.authorization.adapter.inbound.rest.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class GetActionsResponse(
    val actions: List<GetActionDto>
)
