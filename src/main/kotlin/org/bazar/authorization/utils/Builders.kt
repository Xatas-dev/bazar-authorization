package org.bazar.authorization.utils

import org.bazar.authorization.database.entity.*
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.model.authz.AuthorizationRequest
import org.bazar.authorization.model.rest.response.GetActionsResponse
import org.bazar.authorization.model.rest.response.GetRoleNameDto
import org.bazar.authorization.model.rest.response.GetRoleNamesResponse
import org.bazar.authorization.utils.extensions.toGetActionDto
import java.util.*

fun buildSpaceUser(spaceId: Long, userId: UUID, roleId: Long, creator: Boolean): SpaceUserEntity {
    return SpaceUserEntity(
        spaceId = spaceId,
        userId = userId,
        roleId = roleId,
        creator = creator
    )
}

fun buildRolesActions(roleId: Long, actionId: Int): RolesActionsEntity {
    return RolesActionsEntity(
        roleId = roleId,
        actionId = actionId,
        null
    )
}

fun buildRole(scope: RoleScope, name: String, spaceId: Long? = null): RoleEntity {
    return RoleEntity(
        name = name,
        spaceId = spaceId,
        scope = scope
    )
}

fun buildAuthorizationRequest(
    spaceUser: SpaceUserEntity,
    resource: String,
    action: String,
    attributes: Map<String, String>? = null
): AuthorizationRequest {
    return AuthorizationRequest(
        spaceUser.spaceId,
        spaceUser.userId,
        spaceUser.creator,
        resource,
        action,
        attributes
    )
}

fun buildGetActionsResponse(actions: List<ActionEntity>, attributes: List<ActionAttributeEntity>): GetActionsResponse {
    val actionsDto = actions.map { action ->
        action.toGetActionDto(attributes.filter { it.actionId == action.id })
    }
    return GetActionsResponse(actionsDto)
}

fun buildGetRoleNamesResponse(userIdToRoleNameMap: Map<UUID, String>) =
    GetRoleNamesResponse(
        roles = userIdToRoleNameMap.map { GetRoleNameDto(it.value, it.key.toString()) }
    )