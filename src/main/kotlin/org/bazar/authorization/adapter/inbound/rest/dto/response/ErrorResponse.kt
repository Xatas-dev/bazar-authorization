package org.bazar.authorization.adapter.inbound.rest.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val code: Int,
    val message: String
)