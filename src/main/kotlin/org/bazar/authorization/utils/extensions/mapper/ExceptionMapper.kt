package org.bazar.authorization.utils.extensions.mapper

import org.bazar.authorization.model.rest.response.ErrorResponse
import org.bazar.authorization.utils.exceptions.ApiException

fun ApiException.toErrorDto() =
    ErrorResponse(
        code = this.exceptionType.httpStatus.value,
        message = this.exceptionType.displayMessage
    )