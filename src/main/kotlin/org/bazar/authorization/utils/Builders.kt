package org.bazar.authorization.utils

import org.bazar.authorization.database.entity.RoleEntity
import org.bazar.authorization.database.entity.RolesActionsEntity
import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.model.authz.AuthorizationRequest
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

fun buildRole(scope: RoleScope, name: String? = null, spaceId: Long? = null): RoleEntity {
    return RoleEntity(
        name = name,
        spaceId = spaceId,
        scope = scope
    )
}

fun buildAuthorizationRequest(
    roleActionMapping: RolesActionsEntity,
    spaceUser: SpaceUserEntity,
    resource: String,
    action: String
): AuthorizationRequest {
    return AuthorizationRequest(
        spaceUser.spaceId,
        spaceUser.userId,
        spaceUser.creator,
        resource,
        action,
        roleActionMapping.assignedAttribute
    )
}