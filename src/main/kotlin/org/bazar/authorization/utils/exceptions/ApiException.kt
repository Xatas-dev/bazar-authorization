package org.bazar.authorization.utils.exceptions

import io.ktor.http.HttpStatusCode


class ApiException(
    val exceptionType: ApiExceptions,
    customMessage: String = ""
) : RuntimeException("${exceptionType.message}: $customMessage")

enum class ApiExceptions(
    val message: String,
    val httpStatus: HttpStatusCode,
    val displayMessage: String
) {
    ILLEGAL_ARGUMENT("Illegal Argument: ", HttpStatusCode.BadRequest, "Ошибка валидации"),
    NO_SUCH_USER_IN_SPACE("No such user found in space: ", HttpStatusCode.Forbidden, "Пользователя нет в этом спейсе"),
    NO_SUCH_ACTION("No such authorization action", HttpStatusCode.Forbidden, "Такого экшена нет в системе"),
    NO_SUCH_ROLE("No such role", HttpStatusCode.BadRequest, "Такой роли не сущестует"),
    INSUFFICIENT_PERMISSIONS("No permission to access this resource", HttpStatusCode.Forbidden, "Недостаточно прав"),
    USER_ALREADY_EXISTS("User already exists in space", HttpStatusCode.Conflict, "Такой пользователь уже есть"),
    NO_SUCH_ACTION_IN_ROLE("No such action in role", HttpStatusCode.Forbidden, "Недостаточно прав"),
    NO_SUCH_ATTRIBUTE("No such attribute: ", HttpStatusCode.BadRequest, "Аттрибута не существует")
}
