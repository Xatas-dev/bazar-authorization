package org.bazar.authorization.controller

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.bazar.authorization.model.rest.request.CreateRoleRequest
import org.bazar.authorization.model.rest.request.PutRoleRequest
import org.bazar.authorization.service.AuthorizationService
import org.bazar.authorization.service.api.RolesApiService
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.builder.buildAuthorizationCommand
import org.bazar.authorization.utils.extensions.getAuthenticatedUserId
import org.bazar.authorization.utils.extensions.mapper.extractActionsToGrant
import java.util.*

class RolesController(
    private val authorizationService: AuthorizationService,
    private val rolesApiService: RolesApiService
) {

    fun Route.getRoles() = get("/roles") {
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val requesterId = call.getAuthenticatedUserId()

        val authorizeCommand = buildAuthorizationCommand(
            spaceId,
            requesterId,
            "roles",
            "READ"
        )

        if (!authorizationService.authorize(authorizeCommand)
        ) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
        }

        call.respond(
            message = rolesApiService.getRoles(spaceId)
        )
    }

    fun Route.getSingleEnrichedRole() = get("/roles/{roleId}") {
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val roleId = call.pathParameters.getOrFail<Long>("roleId")
        val requesterId = call.getAuthenticatedUserId()

        val authorizeCommand = buildAuthorizationCommand(
            spaceId,
            requesterId,
            "roles",
            "READ",
            resourceId = roleId.toString()
        )

        if (!authorizationService.authorize(authorizeCommand)
        ) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
        }

        call.respond(
            message = rolesApiService.getRoleWithActionsAndAttributes(roleId)
        )
    }

    fun Route.createRole() = post("/roles") {
        val request = call.receive<CreateRoleRequest>()
        val requesterId = call.getAuthenticatedUserId()

        val authorizeCommand = buildAuthorizationCommand(
            request.spaceId,
            requesterId,
            "roles",
            "CREATE",
            principalAttributes = request.extractActionsToGrant()
        )

        if (!authorizationService.authorize(authorizeCommand)
        ) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
        }

        call.respond(
            rolesApiService.createRole(request, requesterId)
        )
    }

    fun Route.putRole() = put("/roles") {
        val request = call.receive<PutRoleRequest>()
        val requesterId = call.getAuthenticatedUserId()
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val roleId = call.queryParameters.getOrFail<Long>("roleId")

        val authorizeCommand = buildAuthorizationCommand(
            spaceId,
            requesterId,
            "roles",
            "EDIT",
            resourceId = roleId.toString(),
            principalAttributes = request.extractActionsToGrant()
        )

        if (!authorizationService.authorize(authorizeCommand)
        ) {
            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
        }

        call.respond(
            rolesApiService.updateRole(request, roleId)
        )
    }

}