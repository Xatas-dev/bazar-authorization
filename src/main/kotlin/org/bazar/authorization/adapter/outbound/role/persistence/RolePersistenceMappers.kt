package org.bazar.authorization.adapter.outbound.role.persistence

import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.application.role.RoleActionMapping
import org.bazar.authorization.infrastructure.util.extension.toUuid
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toRole(): Role = Role(
    id = this[Roles.id].value,
    name = this[Roles.name],
    spaceId = this[Roles.spaceId],
    scope = this[Roles.scope],
    isVisible = this[Roles.isVisible],
    createdBy = this[Roles.createdBy]?.toUuid(),
    createdAt = this[Roles.createdAt],
    updatedAt = this[Roles.updatedAt]
)

fun ResultRow.toRoleActionMapping(): RoleActionMapping = RoleActionMapping(
    roleId = this[RolesActions.role].value,
    actionId = this[RolesActions.action].value,
    assignedAttributes = this[RolesActions.assignedAttribute],
    createdAt = this[RolesActions.createdAt],
    updatedAt = this[RolesActions.updatedAt]
)
