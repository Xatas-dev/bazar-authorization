package org.bazar.authorization.database.entity

data class RoleWithActionMappings(
    val role: RoleEntity,
    val actionMappings: List<RolesActionsEntity>
)
