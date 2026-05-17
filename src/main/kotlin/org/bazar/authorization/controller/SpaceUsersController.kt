package org.bazar.authorization.controller

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.bazar.authorization.service.AuthorizationService
import org.bazar.authorization.service.api.SpaceUserApiService
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.builder.buildAuthorizationCommand
import org.bazar.authorization.utils.extensions.getAuthenticatedUserId
import java.util.*

class SpaceUsersController(
    private val authorizationService: AuthorizationService,
    private val spaceUserApiService: SpaceUserApiService
) {

    fun Route.patchSpaceUsersRole() = patch("/space-users/roles") {
        val requesterId = call.getAuthenticatedUserId()
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val roleId = call.queryParameters.getOrFail<Long>("roleId")
        val targetUserId = call.queryParameters.getOrFail<UUID>("userId")

        val authorizationCommand = buildAuthorizationCommand(
            spaceId,
            requesterId,
            "roles",
            "ASSIGN",
            roleId.toString()
        )

        if (!authorizationService.authorize(authorizationCommand)
        ) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
        }

        spaceUserApiService.assignRoleToUser(spaceId, targetUserId, roleId)
        call.respond(HttpStatusCode.OK)
    }

    fun Route.getRoleNames() = get("/space-users/roles") {
        val userIds = call.queryParameters.getOrFail<List<UUID>>("userIds")
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val requesterId = call.getAuthenticatedUserId()

        authorizationService.checkIfUserInSpace(requesterId, spaceId)

        call.respond(
            spaceUserApiService.getRoleNames(spaceId, userIds)
        )
    }

}