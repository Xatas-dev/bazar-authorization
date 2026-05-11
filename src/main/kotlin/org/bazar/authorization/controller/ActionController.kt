package org.bazar.authorization.controller

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.bazar.authorization.service.AuthorizationService
import org.bazar.authorization.service.api.ActionApiService
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.getAuthenticatedUserId
import org.bazar.authorization.utils.extensions.builder.buildAuthorizationCommand

class ActionController(
    private val actionApiService: ActionApiService,
    private val authorizationService: AuthorizationService
) {

    fun Route.getAllActions() = get("/actions") {
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")

        val authorizeCommand = buildAuthorizationCommand(
            spaceId, call.getAuthenticatedUserId(), "roles", "READ"
        )

        if (!authorizationService.authorize(authorizeCommand)) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
        }

        call.respond(actionApiService.getActionsWithAttributes())
    }

}