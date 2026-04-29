package org.bazar.authorization.database.repository

import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.database.tables.SpaceUsers
import org.bazar.authorization.utils.extensions.toSpaceUserEntity
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.*

class SpaceUserRepository {

    fun deleteUsers(spaceId: Long): Int {
        return SpaceUsers.deleteWhere {
            this.spaceId eq spaceId
        }
    }

    fun findAllRoleIds(spaceId: Long): List<Long> {
        return SpaceUsers.select(column = SpaceUsers.role)
            .where(SpaceUsers.spaceId eq spaceId)
            .map { it[SpaceUsers.role].value }

    }

    fun findRoleIdBySpaceIdAndUserId(spaceId: Long, userId: UUID): SpaceUserEntity? {
        return SpaceUsers.selectAll()
            .where { (SpaceUsers.spaceId eq spaceId) and (SpaceUsers.userId eq userId.toString()) }
            .singleOrNull()
            ?.toSpaceUserEntity()
    }

    fun save(entity: SpaceUserEntity) {
        SpaceUsers.upsert {
            entity.id?.let { entityId -> it[id] = entityId }
            it[spaceId] = entity.spaceId
            it[userId] = entity.userId.toString()
            it[role] = entity.roleId
            it[creator] = entity.creator
            it[createdAt] = entity.createdAt
            it[updatedAt] = entity.updatedAt
        }
    }

    fun deleteSpaceUser(spaceId: Long, userId: UUID) {
        SpaceUsers.deleteWhere {
            SpaceUsers.spaceId eq spaceId
            SpaceUsers.userId eq userId.toString()
        }
    }

    fun findAll(): List<SpaceUserEntity> {
        return SpaceUsers.selectAll()
            .map { it.toSpaceUserEntity() }
    }

}