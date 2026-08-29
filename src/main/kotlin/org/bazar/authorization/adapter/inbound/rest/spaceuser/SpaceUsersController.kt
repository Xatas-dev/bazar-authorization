package org.bazar.authorization.adapter.inbound.rest.spaceuser

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetRoleNamesResponse
import org.bazar.authorization.application.shared.AuthenticatedUserProvider
import org.bazar.authorization.application.spaceuser.command.AssignRoleCommand
import org.bazar.authorization.application.spaceuser.port.`in`.AssignRoleToUserUseCase
import org.bazar.authorization.application.spaceuser.port.`in`.GetRoleNamesUseCase
import org.bazar.authorization.application.spaceuser.query.GetRoleNamesQuery
import java.util.*

class SpaceUsersController(
    private val assignRoleToUserUseCase: AssignRoleToUserUseCase,
    private val getRoleNamesUseCase: GetRoleNamesUseCase,
    private val authenticatedUserProvider: AuthenticatedUserProvider
) {

    fun Route.patchSpaceUsersRole() = patch("/space-users/roles") {
        val requesterId = authenticatedUserProvider.getUserId()
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val roleId = call.queryParameters.getOrFail<Long>("roleId")
        val targetUserId = call.queryParameters.getOrFail<UUID>("userId")

        assignRoleToUserUseCase.execute(AssignRoleCommand(spaceId, requesterId, targetUserId, roleId))

        call.respond(HttpStatusCode.OK)
    }

    fun Route.getRoleNames() = get("/space-users/roles") {
        val userIds = call.queryParameters.getOrFail<List<UUID>>("userIds")
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val requesterId = authenticatedUserProvider.getUserId()

        val roleNames = getRoleNamesUseCase.execute(GetRoleNamesQuery(spaceId, requesterId, userIds))

        call.respond(
            GetRoleNamesResponse(roles = roleNames.map { it.toGetRoleNameDto() })
        )
    }
}
