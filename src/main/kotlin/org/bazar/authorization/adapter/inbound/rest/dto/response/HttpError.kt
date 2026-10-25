package org.bazar.authorization.adapter.inbound.rest.dto.response

import io.ktor.http.*

data class HttpError(
    val status: HttpStatusCode,
    val body: ApiErrorResponse
)