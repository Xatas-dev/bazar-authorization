package org.bazar.authorization.infrastructure.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.bazar.authorization.adapter.inbound.rest.toErrorDto
import org.bazar.authorization.adapter.inbound.rest.toHttpStatusCode
import org.bazar.authorization.domain.exception.DomainException
import org.bazar.authorization.adapter.inbound.rest.dto.response.ErrorResponse
import org.bazar.authorization.infrastructure.util.logger

fun Application.configureStatusPages() {
    val logger = logger()
    install(StatusPages) {
        exception<DomainException> { call, cause ->
            logger.error(cause.message, cause)
            call.respond(status = cause.toHttpStatusCode(), cause.toErrorDto())
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
