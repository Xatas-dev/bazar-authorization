package org.bazar.authorization.infrastructure.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.bazar.authorization.adapter.inbound.rest.dto.response.ApiErrorResponse
import org.bazar.authorization.adapter.inbound.rest.errors.toHttpError
import org.bazar.authorization.domain.exception.DomainException
import org.bazar.authorization.infrastructure.util.logger

fun Application.configureStatusPages() {
    val logger = logger()
    install(StatusPages) {
        exception<DomainException> { call, cause ->
            val httpError = cause.toHttpError()
            if (httpError.status.value >= 500) {
                logger.error(cause.message, cause)
            } else {
                logger.warn(cause.message)
            }
            call.respond(httpError.status, httpError.body)
        }

        exception<BadRequestException> { call, cause ->
            logger.warn(cause.message)
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrorResponse("VALIDATION_ERROR", "Ошибка валидации")
            )
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrorResponse("INTERNAL_ERROR", "Внутренняя ошибка сервера")
            )
        }
    }
}
