package org.bazar.authorization.controller

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.bazar.authorization.model.rest.request.CreateRoleRequest
import org.bazar.authorization.service.AuthorizationService
import org.bazar.authorization.service.api.SpaceUserApiService
import org.bazar.authorization.utils.authorization.enums.Permission.READ_ACTIONS
import org.bazar.authorization.utils.authorization.enums.Permission.WRITE_ACTIONS
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.extractActionsToGrant
import org.bazar.authorization.utils.extensions.getAuthenticatedUserId
import org.bazar.authorization.utils.extensions.toUuid
import java.util.UUID

class SpaceUserController(
    private val authorizationService: AuthorizationService,
    private val spaceUserApiService: SpaceUserApiService
) {

    fun Route.getRole() = get("/space-users/roles") {
        val userId = call.queryParameters.getOrFail<String>("userId").toUuid()
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val requesterId = call.getAuthenticatedUserId()

        if (userId != requesterId &&
            !authorizationService.authorize(spaceId, requesterId, READ_ACTIONS)
        ) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
        }

        call.respond(
            spaceUserApiService.getRoleWithActionsAndAttributes(spaceId, userId)
        )
    }

    fun Route.getRoleNames() = get("/space-users/role-names") {
        val userIds = call.queryParameters.getOrFail<List<UUID>>("userIds")
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val requesterId = call.getAuthenticatedUserId()

        authorizationService.checkIfUserInSpace(requesterId, spaceId)

        call.respond(
            spaceUserApiService.getRoleNames(spaceId, userIds)
        )
    }

    fun Route.createRole() = post("/space-users/roles") {
        val request = call.receive<CreateRoleRequest>()
        val requesterId = call.getAuthenticatedUserId()

        if (!authorizationService.authorize(
                request.spaceId,
                requesterId,
                WRITE_ACTIONS,
                request.extractActionsToGrant()
            )
        ) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
        }

        call.respond(
            spaceUserApiService.createRoleAndDeletePrev(request)
        )
    }

}