package org.bazar.authorization.application.role.port.out

import org.bazar.authorization.application.role.RoleActionMapping

interface RolesActionsRepositoryPort {
    fun saveAll(mappings: List<RoleActionMapping>): List<RoleActionMapping>

    fun findAllByRoleId(roleId: Long): List<RoleActionMapping>

    fun deleteAll(roleIds: List<Long>)
}
