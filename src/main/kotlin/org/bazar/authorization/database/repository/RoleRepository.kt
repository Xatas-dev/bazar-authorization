package org.bazar.authorization.database.repository

import org.bazar.authorization.database.entity.RoleEntity
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.database.tables.Roles
import org.bazar.authorization.utils.extensions.toRoleEntity
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class RoleRepository {

    fun deleteAll(roleIds: List<Long>) {
        Roles.deleteWhere { Roles.id inList roleIds }
    }

    fun save(entity: RoleEntity): RoleEntity {
        return Roles.insertReturning {
            it[name] = entity.name
            it[spaceId] = entity.spaceId
            it[scope] = entity.scope
            it[createdAt] = entity.createdAt
            it[updatedAt] = entity.updatedAt
        }.single().toRoleEntity()
    }

    fun getAllByScopeAndRoleIdsIn(scope: RoleScope, roleIds: List<Long>): List<RoleEntity> {
        return Roles.selectAll()
            .where { (Roles.scope eq scope) and (Roles.id inList roleIds) }
            .map { it.toRoleEntity() }
    }

    fun getAllByRoleIdsIn(roleIds: Collection<Long>): List<RoleEntity> {
        return Roles.selectAll()
            .where { Roles.id inList roleIds }
            .map { it.toRoleEntity() }
    }

    fun getAllRoles(): List<RoleEntity> {
        return Roles.selectAll()
            .map { it.toRoleEntity() }
    }

    suspend fun findById(id: Long): RoleEntity? = suspendTransaction {
        Roles.selectAll()
            .where { Roles.id eq id }
            .singleOrNull()?.toRoleEntity()
    }

}