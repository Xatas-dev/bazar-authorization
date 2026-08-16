package org.bazar.authorization.adapter.inbound.rest.spaceuser

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.util.getOrFail
import org.bazar.authorization.adapter.inbound.rest.getAuthenticatedUserId
import org.bazar.authorization.application.spaceuser.command.AssignRoleCommand
import org.bazar.authorization.application.spaceuser.port.`in`.AssignRoleToUserUseCase
import org.bazar.authorization.application.spaceuser.port.`in`.GetRoleNamesUseCase
import org.bazar.authorization.application.spaceuser.query.GetRoleNamesQuery
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetRoleNamesResponse
import java.util.UUID

class SpaceUsersController(
    private val assignRoleToUserUseCase: AssignRoleToUserUseCase,
    private val getRoleNamesUseCase: GetRoleNamesUseCase
) {

    fun Route.patchSpaceUsersRole() = patch("/space-users/roles") {
        val requesterId = call.getAuthenticatedUserId()
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val roleId = call.queryParameters.getOrFail<Long>("roleId")
        val targetUserId = call.queryParameters.getOrFail<UUID>("userId")

        assignRoleToUserUseCase.execute(AssignRoleCommand(spaceId, requesterId, targetUserId, roleId))

        call.respond(HttpStatusCode.OK)
    }

    fun Route.getRoleNames() = get("/space-users/roles") {
        val userIds = call.queryParameters.getOrFail<List<UUID>>("userIds")
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val requesterId = call.getAuthenticatedUserId()

        val roleNames = getRoleNamesUseCase.execute(GetRoleNamesQuery(spaceId, requesterId, userIds))

        call.respond(
            GetRoleNamesResponse(roles = roleNames.map { it.toGetRoleNameDto() })
        )
    }
}
