package org.bazar.authorization.utils.extensions.builder

import org.bazar.authorization.database.entity.ActionAttributeEntity
import org.bazar.authorization.database.entity.ActionEntity
import org.bazar.authorization.database.entity.RolesActionsEntity
import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.model.authz.AuthorizationRequest
import org.bazar.authorization.model.rest.response.GetActionsResponse
import org.bazar.authorization.utils.extensions.mapper.toGetActionDto
import java.util.*

fun buildSpaceUser(spaceId: Long, userId: UUID, roleId: Long, isCreator: Boolean): SpaceUserEntity {
    return SpaceUserEntity(
        spaceId = spaceId,
        userId = userId,
        roleId = roleId,
        isCreator = isCreator
    )
}

fun buildRolesActions(roleId: Long, actionId: Int): RolesActionsEntity {
    return RolesActionsEntity(
        roleId = roleId,
        actionId = actionId,
        null
    )
}

fun buildAuthorizationRequest(
    spaceUser: SpaceUserEntity,
    resource: String,
    action: String,
    principalAttributes: Map<String, String> = emptyMap(),
    resourceAttributes: Map<String, String> = emptyMap()
): AuthorizationRequest {
    return AuthorizationRequest(
        spaceUser.spaceId,
        spaceUser.userId,
        resource,
        action,
        principalAttributes,
        resourceAttributes
    )
}

fun buildGetActionsResponse(actions: List<ActionEntity>, attributes: List<ActionAttributeEntity>): GetActionsResponse {
    val actionsDto = actions.map { action ->
        action.toGetActionDto(attributes.filter { it.actionId == action.id })
    }
    return GetActionsResponse(actionsDto)
}