package org.bazar.authorization.application.role.port.out

import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.domain.role.RoleScope

interface RoleRepositoryPort {
    fun save(role: Role): Role

    fun update(role: Role): Role

    fun findById(id: Long): Role?

    fun findByScopeAndIdsIn(scope: RoleScope, roleIds: List<Long>): List<Role>

    fun findByIdsIn(roleIds: Collection<Long>): List<Role>

    fun findAllBySpaceId(spaceId: Long): List<Role>

    fun deleteAll(roleIds: List<Long>)
}
