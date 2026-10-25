package org.bazar.authorization.adapter.inbound.rest.errors

import io.ktor.http.*
import org.bazar.authorization.adapter.inbound.rest.dto.response.ApiErrorResponse
import org.bazar.authorization.adapter.inbound.rest.dto.response.HttpError
import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.exception.*

fun DomainException.toHttpError(): HttpError = when (this) {
    is UnauthenticatedException ->
        httpError(HttpStatusCode.Unauthorized, "UNAUTHENTICATED", "Требуется авторизация")

    is InsufficientPermissionsException ->
        httpError(HttpStatusCode.Forbidden, "INSUFFICIENT_PERMISSIONS", "Недостаточно прав")

    is UserNotInSpaceException ->
        httpError(HttpStatusCode.Forbidden, "USER_NOT_IN_SPACE", "Вы не состоите в этом спейсе")

    is UserAlreadyInSpaceException ->
        httpError(HttpStatusCode.Conflict, "USER_ALREADY_IN_SPACE", "Пользователь уже состоит в спейсе")

    is RoleNotFoundException ->
        httpError(HttpStatusCode.NotFound, "ROLE_NOT_FOUND", "Роль не найдена")

    is ActionsNotFoundException ->
        httpError(HttpStatusCode.BadRequest, "ACTIONS_NOT_FOUND", "Некоторые действия не существуют")

    is InvalidArgumentException ->
        httpError(HttpStatusCode.BadRequest, "VALIDATION_ERROR", reason)

    is UnsatisfiedActionDependenciesException ->
        httpError(
            HttpStatusCode.UnprocessableEntity,
            "UNSATISFIED_ACTION_DEPENDENCIES",
            dependenciesMessage(originalAction, missingDependencyAction)
        )

    else ->
        httpError(
            HttpStatusCode.InternalServerError,
            "INTERNAL_ERROR",
            "Внутренняя ошибка сервера"
        )
}

private fun dependenciesMessage(
    originalAction: Action, missingDependency: Action
): String = "Для «${originalAction.name}» необходимо также добавить «${missingDependency.name}»"

private fun httpError(
    status: HttpStatusCode,
    code: String,
    message: String
): HttpError =
    HttpError(
        status = status,
        body = ApiErrorResponse(
            code = code,
            message = message
        )
    )