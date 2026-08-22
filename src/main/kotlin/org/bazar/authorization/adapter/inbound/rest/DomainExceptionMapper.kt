package org.bazar.authorization.adapter.inbound.rest

import io.ktor.http.HttpStatusCode
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.adapter.inbound.rest.dto.response.ErrorResponse

private fun DomainErrors.toHttpStatusCode(): HttpStatusCode = when (this) {
    DomainErrors.ILLEGAL_ARGUMENT -> HttpStatusCode.BadRequest
    DomainErrors.NO_SUCH_USER_IN_SPACE -> HttpStatusCode.Forbidden
    DomainErrors.NO_SUCH_ROLE -> HttpStatusCode.BadRequest
    DomainErrors.INSUFFICIENT_PERMISSIONS -> HttpStatusCode.Forbidden
    DomainErrors.USER_ALREADY_EXISTS -> HttpStatusCode.Conflict
    DomainErrors.NO_SUCH_ATTRIBUTE -> HttpStatusCode.BadRequest
    DomainErrors.UNAUTHENTICATED -> HttpStatusCode.Unauthorized
}

private val DomainErrors.displayMessage: String
    get() = when (this) {
        DomainErrors.ILLEGAL_ARGUMENT -> "Ошибка валидации"
        DomainErrors.NO_SUCH_USER_IN_SPACE -> "Пользователя нет в этом спейсе"
        DomainErrors.NO_SUCH_ROLE -> "Такой роли не существует"
        DomainErrors.INSUFFICIENT_PERMISSIONS -> "Недостаточно прав"
        DomainErrors.USER_ALREADY_EXISTS -> "Такой пользователь уже есть"
        DomainErrors.NO_SUCH_ATTRIBUTE -> "Аттрибута не существует"
        DomainErrors.UNAUTHENTICATED -> "Пользователь не авторизовался"
    }

fun org.bazar.authorization.domain.exception.DomainException.toHttpStatusCode(): HttpStatusCode =
    exceptionType.toHttpStatusCode()

fun org.bazar.authorization.domain.exception.DomainException.toErrorDto() =
    ErrorResponse(
        code = this.exceptionType.toHttpStatusCode().value,
        message = this.exceptionType.displayMessage
    )
