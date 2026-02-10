package org.bazar.authorization.database.repository

import org.bazar.authorization.database.entity.UserSpaceRole
import org.bazar.authorization.database.entity.UserSpaceRoleTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.*

class UserSpaceRoleRepository {

    fun save(entity: UserSpaceRole) = transaction {
        UserSpaceRoleTable.upsert {
            it[spaceId] = entity.spaceId
            it[userId] = entity.userId
            it[role] = entity.role
            it[createdAt] = entity.createdAt
            it[updatedAt] = entity.updatedAt
        }
    }

    fun findById(spaceId: Long, userId: UUID): UserSpaceRole? = transaction {
         UserSpaceRoleTable
            .selectAll()
            .where { (UserSpaceRoleTable.spaceId eq spaceId) and (UserSpaceRoleTable.userId eq userId) }
            .map { it.toUserSpaceRole() }
            .singleOrNull()
    }

    fun deleteBySpaceIdAndUserId(spaceId: Long, userId: UUID): Int = transaction {
        UserSpaceRoleTable.deleteWhere {
            (UserSpaceRoleTable.spaceId eq spaceId) and (UserSpaceRoleTable.userId eq userId)
        }
    }

    fun deleteAllBySpaceId(spaceId: Long): Int = transaction {
        UserSpaceRoleTable.deleteWhere { UserSpaceRoleTable.spaceId eq spaceId }
    }

    fun findAll(): List<UserSpaceRole> = transaction {
        UserSpaceRoleTable.selectAll().map { it.toUserSpaceRole() }
    }

    private fun ResultRow.toUserSpaceRole() = UserSpaceRole(
        spaceId = this[UserSpaceRoleTable.spaceId],
        userId = this[UserSpaceRoleTable.userId],
        role = this[UserSpaceRoleTable.role],
        createdAt = this[UserSpaceRoleTable.createdAt],
        updatedAt = this[UserSpaceRoleTable.updatedAt]
    )
}