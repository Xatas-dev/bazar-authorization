package org.bazar.authorization.utils.extensions

import org.bazar.authorization.database.entity.RoleEntity
import org.bazar.authorization.database.entity.RoleWithActionMappings
import org.bazar.authorization.database.entity.RolesActionsEntity

fun RoleEntity.toRoleWithActionMappings(mappings: List<RolesActionsEntity>): RoleWithActionMappings {
    return RoleWithActionMappings(
        role = this,
        actionMappings = mappings
    )
}

