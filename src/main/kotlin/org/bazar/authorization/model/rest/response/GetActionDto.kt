package org.bazar.authorization.model.rest.response

import kotlinx.serialization.Serializable

@Serializable
data class GetActionDto(
    val id: Int,
    val code: String,
    val name: String,
    val resource: String,
    val attributes: List<GetActionAttributeDto>
)
