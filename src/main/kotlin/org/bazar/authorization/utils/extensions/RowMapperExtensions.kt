package org.bazar.authorization.utils.extensions

import org.bazar.authorization.database.entity.ActionAttributeEntity
import org.bazar.authorization.database.entity.ActionEntity
import org.bazar.authorization.database.entity.RoleEntity
import org.bazar.authorization.database.entity.RolesActionsEntity
import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.database.tables.ActionAttributes
import org.bazar.authorization.database.tables.Actions
import org.bazar.authorization.database.tables.Roles
import org.bazar.authorization.database.tables.RolesActions
import org.bazar.authorization.database.tables.SpaceUsers
import org.jetbrains.exposed.v1.core.ResultRow


fun ResultRow.toActionEntity(): ActionEntity {
    return ActionEntity(
        id = this[Actions.id].value,
        code = this[Actions.code],
        name = this[Actions.name],
        resource = this[Actions.resource],
        createdAt = this[Actions.createdAt],
        updatedAt = this[Actions.updatedAt]
    )
}

fun ResultRow.toRolesActionsEntity(): RolesActionsEntity {
    return RolesActionsEntity(
        roleId = this[RolesActions.role].value,
        actionId = this[RolesActions.action].value,
        assignedAttributes = this[RolesActions.assignedAttribute],
        createdAt = this[RolesActions.createdAt],
        updatedAt = this[RolesActions.updatedAt]
    )
}

fun ResultRow.toSpaceUserEntity(): SpaceUserEntity {
    return SpaceUserEntity(
        id = this[SpaceUsers.id].value,
        spaceId = this[SpaceUsers.spaceId],
        userId = this[SpaceUsers.userId].toUuid(),
        roleId = this[SpaceUsers.role].value,
        creator = this[SpaceUsers.creator],
        createdAt = this[SpaceUsers.createdAt],
        updatedAt = this[SpaceUsers.updatedAt]
    )
}

fun ResultRow.toRoleEntity(): RoleEntity {
    return RoleEntity(
        id = this[Roles.id].value,
        name = this[Roles.name],
        spaceId = this[Roles.spaceId],
        scope = this[Roles.scope],
        createdAt = this[Roles.createdAt],
        updatedAt = this[Roles.updatedAt]
    )
}

fun ResultRow.toActionAttribute(): ActionAttributeEntity {
    return ActionAttributeEntity(
        id = this[ActionAttributes.id].value,
        actionId = this[ActionAttributes.action].value,
        name = this[ActionAttributes.name],
        displayName = this[ActionAttributes.displayName],
        valueType = this[ActionAttributes.valueType],
        createdAt = this[ActionAttributes.createdAt],
        updatedAt = this[ActionAttributes.updatedAt]
    )
}