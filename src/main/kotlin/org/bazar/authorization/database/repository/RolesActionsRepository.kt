package org.bazar.authorization.database.repository

import org.bazar.authorization.database.entity.RolesActionsEntity
import org.bazar.authorization.database.tables.RolesActions
import org.bazar.authorization.utils.extensions.mapper.toRolesActionsEntity
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

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
            it[RolesActions.assignedAttribute] = entity.assignedAttributes
            it[RolesActions.createdAt] = entity.createdAt
            it[RolesActions.updatedAt] = entity.updatedAt
        }
    }

    suspend fun saveAll(entities: List<RolesActionsEntity>) = suspendTransaction {
        RolesActions.batchInsert(entities) {
            this[RolesActions.role] = it.roleId
            this[RolesActions.action] = it.actionId
            this[RolesActions.assignedAttribute] = it.assignedAttributes
            this[RolesActions.createdAt] = it.createdAt
            this[RolesActions.updatedAt] = it.updatedAt
        }.map { it.toRolesActionsEntity() }
    }

    fun getAllRoleActionMappings(): List<RolesActionsEntity> {
        return RolesActions.selectAll()
            .map { it.toRolesActionsEntity() }

    }

    suspend fun findAllByRoleId(roleId: Long) = suspendTransaction {
        RolesActions.selectAll()
            .where { RolesActions.role eq roleId }
            .map { it.toRolesActionsEntity() }
    }

}