package org.bazar.authorization.utils.exceptions

import io.netty.handler.codec.http.HttpResponseStatus


class ApiException(
    val exceptionType: ApiExceptions,
    customMessage: String = ""
) : RuntimeException("${exceptionType.message}: $customMessage")

enum class ApiExceptions(
    val message: String,
    val httpStatus: HttpResponseStatus
) {
    ILLEGAL_ARGUMENT("Illegal Argument: ", HttpResponseStatus.BAD_REQUEST),
    NO_SUCH_USER_IN_SPACE("No such user found in space: ", HttpResponseStatus.FORBIDDEN),
    NO_SUCH_ACTION("No such authorization action", HttpResponseStatus.BAD_REQUEST),
    INSUFFICIENT_PERMISSIONS("No permission to access this resource", HttpResponseStatus.FORBIDDEN),
    ALREADY_EXISTS("User already exists in space", HttpResponseStatus.CONFLICT),
    NO_SUCH_ACTION_IN_ROLE("No such action in role", HttpResponseStatus.FORBIDDEN)
}
