package org.bazar.authorization.database.repository

import org.bazar.authorization.database.entity.RolesActionsEntity
import org.bazar.authorization.database.tables.RolesActions
import org.bazar.authorization.utils.extensions.toRolesActionsEntity
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

class RolesActionsRepository {

    fun deleteAll(roleIds: List<Long>): Int {
        return RolesActions.deleteWhere { RolesActions.role inList roleIds }
    }

    fun findByRoleIdAndActionId(roleId: Long, actionId: Int): RolesActionsEntity? {
        return RolesActions.selectAll()
            .where { (RolesActions.role eq roleId) and (RolesActions.action eq actionId) }
            .singleOrNull()?.toRolesActionsEntity()
    }

    fun save(entity: RolesActionsEntity) {
        RolesActions.insert {
            it[RolesActions.role] = entity.roleId
            it[RolesActions.action] = entity.actionId
            it[RolesActions.assignedAttribute] = entity.assignedAttribute
            it[RolesActions.createdAt] = entity.createdAt
            it[RolesActions.updatedAt] = entity.updatedAt
        }
    }

    fun getAllRoleActionMappings(): List<RolesActionsEntity> {
        return RolesActions.selectAll()
            .map { it.toRolesActionsEntity() }

    }

}