package org.bazar.authorization.adapter.inbound.rest.role

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.util.getOrFail
import org.bazar.authorization.application.role.command.CreateRoleCommand
import org.bazar.authorization.application.role.port.`in`.CreateRoleUseCase
import org.bazar.authorization.application.role.port.`in`.GetEnrichedRoleUseCase
import org.bazar.authorization.application.role.port.`in`.GetRolesInSpaceUseCase
import org.bazar.authorization.application.role.port.`in`.UpdateRoleUseCase
import org.bazar.authorization.application.role.query.GetRoleQuery
import org.bazar.authorization.application.role.query.GetRolesQuery
import org.bazar.authorization.application.shared.AuthenticatedUserProvider
import org.bazar.authorization.adapter.inbound.rest.dto.request.CreateRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.request.PutRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetRolesResponse

class RolesController(
    private val getRolesInSpaceUseCase: GetRolesInSpaceUseCase,
    private val getEnrichedRoleUseCase: GetEnrichedRoleUseCase,
    private val createRoleUseCase: CreateRoleUseCase,
    private val updateRoleUseCase: UpdateRoleUseCase,
    private val authenticatedUserProvider: AuthenticatedUserProvider
) {

    fun Route.getRoles() = get("/roles") {
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val requesterId = authenticatedUserProvider.getUserId()

        val roles = getRolesInSpaceUseCase.execute(GetRolesQuery(spaceId, requesterId))

        call.respond(GetRolesResponse(roles.map { it.toGetRoleDto() }))
    }

    fun Route.getSingleEnrichedRole() = get("/roles/{roleId}") {
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val roleId = call.pathParameters.getOrFail<Long>("roleId")
        val requesterId = authenticatedUserProvider.getUserId()

        val role = getEnrichedRoleUseCase.execute(GetRoleQuery(roleId, spaceId, requesterId))

        call.respond(role.toResponse())
    }

    fun Route.createRole() = post("/roles") {
        val request = call.receive<CreateRoleRequest>()
        val requesterId = authenticatedUserProvider.getUserId()

        val command: CreateRoleCommand = request.toCommand(requesterId)

        call.respond(
            createRoleUseCase.execute(command).toResponse()
        )
    }

    fun Route.putRole() = put("/roles") {
        val request = call.receive<PutRoleRequest>()
        val requesterId = authenticatedUserProvider.getUserId()
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val roleId = call.queryParameters.getOrFail<Long>("roleId")

        call.respond(
            updateRoleUseCase.execute(request.toCommand(roleId, spaceId, requesterId)).toResponse()
        )
    }
}
