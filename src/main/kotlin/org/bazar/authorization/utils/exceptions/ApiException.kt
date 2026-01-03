package org.bazar.authorization.utils.exceptions

import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpStatusClass


class ApiException(
    val exceptionType: ApiExceptions,
    val customMessage: String = ""
) : RuntimeException(exceptionType.message + customMessage) {

    fun getFullErrorMessage() = exceptionType.message + customMessage
}

enum class ApiExceptions(
    val message: String,
    val httpStatus: HttpResponseStatus
) {
    ILLEGAL_ARGUMENT("Illegal Argument: ", HttpResponseStatus.BAD_REQUEST),
    NOT_AUTHENTICATED("Not authenticated", HttpResponseStatus.UNAUTHORIZED),
    NO_SUCH_USER_IN_SPACE("No such user found in space: ", HttpResponseStatus.FORBIDDEN),
    SPACE_ALREADY_HAS_CREATOR("Space already has it's creator", HttpResponseStatus.BAD_REQUEST),
    NO_SUCH_ACTION("No such authorization action", HttpResponseStatus.BAD_REQUEST),
    INSUFFICIENT_PERMISSIONS("No permission to access this resource", HttpResponseStatus.FORBIDDEN)
}
