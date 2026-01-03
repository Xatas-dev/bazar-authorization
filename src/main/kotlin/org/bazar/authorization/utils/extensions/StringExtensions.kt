package org.bazar.authorization.utils.extensions

import io.github.oshai.kotlinlogging.KotlinLogging
import org.bazar.authorization.model.authz.enums.AuthorizationAction
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import java.util.UUID

fun String.toUuid() = UUID.fromString(this)

fun String.toAuthorizationAction(): AuthorizationAction {
    try {
        return AuthorizationAction.entries
           .first { this == it.actionName }
    }
    catch (ex: NoSuchElementException){
        throw ApiException(ApiExceptions.NO_SUCH_ACTION)
    }
}