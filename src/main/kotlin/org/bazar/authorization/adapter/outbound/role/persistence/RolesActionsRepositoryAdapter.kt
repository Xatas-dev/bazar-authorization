package org.bazar.authorization.adapter.outbound.role.persistence

import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.role.RoleActionMapping
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchUpsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll

class RolesActionsRepositoryAdapter : RolesActionsRepositoryPort {

    fun save(mapping: RoleActionMapping) {
        RolesActions.batchUpsert(
            data = listOf(mapping),
            RolesActions.role, RolesActions.action,
            onUpdateExclude = listOf(RolesActions.createdAt)
        ) {
            this[RolesActions.role] = it.roleId
            this[RolesActions.action] = it.actionId
            this[RolesActions.assignedAttribute] = it.assignedAttributes
            this[RolesActions.createdAt] = it.createdAt
            this[RolesActions.updatedAt] = it.updatedAt
        }
    }

    override fun saveAll(mappings: List<RoleActionMapping>): List<RoleActionMapping> {
        return RolesActions.batchUpsert(
            data = mappings,
            RolesActions.role, RolesActions.action,
            onUpdateExclude = listOf(RolesActions.createdAt)
        ) {
            this[RolesActions.role] = it.roleId
            this[RolesActions.action] = it.actionId
            this[RolesActions.assignedAttribute] = it.assignedAttributes
            this[RolesActions.createdAt] = it.createdAt
            this[RolesActions.updatedAt] = it.updatedAt
        }.map { it.toRoleActionMapping() }
    }

    override fun findAllByRoleId(roleId: Long): List<RoleActionMapping> {
        return RolesActions.selectAll()
            .where { RolesActions.role eq roleId }
            .map { it.toRoleActionMapping() }
    }

    fun findAll(): List<RoleActionMapping> {
        return RolesActions.selectAll()
            .map { it.toRoleActionMapping() }
    }

    override fun deleteAll(roleIds: List<Long>) {
        RolesActions.deleteWhere { RolesActions.role inList roleIds }
    }
}
