package org.bazar.authorization.adapter.inbound.grpc

import org.bazar.authorization.domain.exception.InvalidArgumentException
import org.bazar.authorization.grpc.AuthorizeRequest
import org.bazar.authorization.grpc.DeleteSpaceRequest
import org.bazar.authorization.grpc.DeleteUserRequest

private fun requireValid(field: String, condition: Boolean, reason: String) {
    if (!condition) throw InvalidArgumentException(field, reason)
}

fun AuthorizeRequest.validate() {
    requireValid("resource", resource.isNotEmpty(), "must not be empty")
    requireValid("action", action.isNotEmpty(), "must not be empty")
    requireValid("spaceId", spaceId != 0L, "must not be empty")
}

fun DeleteSpaceRequest.validate() {
    requireValid("spaceId", spaceId != 0L, "must not be empty")
}

fun DeleteUserRequest.validate() {
    requireValid("userId", !userId.isNullOrBlank(), "must not be blank")
    requireValid("spaceId", spaceId != 0L, "must not be empty")
}
