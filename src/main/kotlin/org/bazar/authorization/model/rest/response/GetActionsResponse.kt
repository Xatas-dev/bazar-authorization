package org.bazar.authorization.model.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class GetActionsResponse(
    val actions: List<GetActionDto>
)
