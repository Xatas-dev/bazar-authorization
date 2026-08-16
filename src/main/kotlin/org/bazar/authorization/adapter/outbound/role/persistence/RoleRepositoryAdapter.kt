package org.bazar.authorization.adapter.outbound.role.persistence

import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.domain.role.RoleScope
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.updateReturning

class RoleRepositoryAdapter : RoleRepositoryPort {

    override fun save(role: Role): Role {
        return Roles.insertReturning {
            it[name] = role.name
            it[spaceId] = role.spaceId
            it[scope] = role.scope
            it[isVisible] = role.isVisible
            it[createdBy] = role.createdBy?.toString()
            it[createdAt] = role.createdAt
            it[updatedAt] = role.updatedAt
        }.single().toRole()
    }

    override fun update(role: Role): Role {
        return Roles.updateReturning(where = { Roles.id eq role.id }) {
            it[name] = role.name
            it[isVisible] = role.isVisible
            it[updatedAt] = role.updatedAt
        }
            .single()
            .toRole()
    }

    override fun findById(id: Long): Role? {
        return Roles.selectAll()
            .where { Roles.id eq id }
            .singleOrNull()?.toRole()
    }

    override fun findByScopeAndIdsIn(scope: RoleScope, roleIds: List<Long>): List<Role> {
        return Roles.selectAll()
            .where { (Roles.scope eq scope) and (Roles.id inList roleIds) }
            .map { it.toRole() }
    }

    override fun findByIdsIn(roleIds: Collection<Long>): List<Role> {
        return Roles.selectAll()
            .where { Roles.id inList roleIds }
            .map { it.toRole() }
    }

    fun findAll(): List<Role> {
        return Roles.selectAll()
            .map { it.toRole() }
    }

    override fun findAllBySpaceId(spaceId: Long): List<Role> {
        return Roles.selectAll()
            .where { Roles.spaceId eq spaceId }
            .map { it.toRole() }
    }

    override fun deleteAll(roleIds: List<Long>) {
        Roles.deleteWhere { Roles.id inList roleIds }
    }
}
