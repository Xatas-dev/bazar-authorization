package org.bazar.authorization.controller

import io.ktor.http.*
import io.ktor.server.request.path
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import org.bazar.authorization.utils.authorization.enums.Permission.READ_ACTIONS
import org.bazar.authorization.service.AuthorizationService
import org.bazar.authorization.service.api.ActionApiService
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.getAuthenticatedUserId

class ActionController(
    private val actionApiService: ActionApiService,
    private val authorizationService: AuthorizationService
) {

    fun Route.getAllActions() = get("/actions") {
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")

        if (!authorizationService.authorize(spaceId, call.getAuthenticatedUserId(), READ_ACTIONS)) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
        }

        call.respond(actionApiService.getActionsWithAttributes())
    }

}