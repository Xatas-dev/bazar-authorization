package org.bazar.authorization.controller

import org.bazar.authorization.service.AuthorizationService
import org.bazar.authorization.service.api.SpaceUserApiService

class SpaceUserController(
    private val authorizationService: AuthorizationService,
    private val spaceUserApiService: SpaceUserApiService
) {

//    fun Route.getRole() = get("/space-users/roles") {
//        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
//        val toAssign = call.queryParameters["toAssign"]?.toBoolean()
//        val requesterId = call.getAuthenticatedUserId()
//
//        val authorizeCommand = buildAuthorizationCommand(
//            spaceId,
//        )
//
//        if (userId != requesterId &&
//            !authorizationService.authorize(spaceId, requesterId, "space_user_actions", "READ")
//        ) {
//            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
//        }
//
//        call.respond(
//            spaceUserApiService.getRoleWithActionsAndAttributes(spaceId, userId)
//        )
//    }

//    fun Route.getRoleNames() = get("/space-users/role-names") {
//        val userIds = call.queryParameters.getOrFail<List<UUID>>("userIds")
//        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
//        val requesterId = call.getAuthenticatedUserId()
//
//        authorizationService.checkIfUserInSpace(requesterId, spaceId)
//
//        call.respond(
//            spaceUserApiService.getRoleNames(spaceId, userIds)
//        )
//    }

//    fun Route.createRole() = post("/space-users/roles") {
//        val request = call.receive<CreateRoleRequest>()
//        val requesterId = call.getAuthenticatedUserId()
//
//        if (!authorizationService.authorize(
//                request.spaceId,
//                requesterId,
//                "space_user_actions",
//                "WRITE",
//                request.extractActionsToGrant()
//            )
//        ) {
//            throw ApiException(ApiExceptions.INSUFFICIENT_PERMISSIONS, "Denied for ${call.request.path()}")
//        }
//
//        call.respond(
//            spaceUserApiService.createRoleAndDeletePrev(request, requesterId)
//        )
//    }

}