package org.bazar.authorization.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.bazar.authorization.model.rest.response.ErrorResponse
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.extensions.toErrorDto
import org.bazar.authorization.utils.logger

fun Application.configureStatusPages() {
    val logger = logger()
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            logger.error(cause.message, cause)
            call.respond(status = cause.exceptionType.httpStatus,cause.toErrorDto())
        }

        exception<BadRequestException> { call, cause ->
            logger.error(cause.message, cause)
            call.respond(status = HttpStatusCode.BadRequest, ErrorResponse(400, "Ошибка валидации"))
        }

        exception<Throwable> { call, cause ->
            logger.error(cause.message, cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(500, "Неизвестная ошибка на стороне сервера")
            )
        }
    }
}