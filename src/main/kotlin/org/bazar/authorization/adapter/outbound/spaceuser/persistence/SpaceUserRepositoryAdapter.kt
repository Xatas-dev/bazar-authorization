package org.bazar.authorization.adapter.outbound.spaceuser.persistence

import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.spaceuser.SpaceUser
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.UUID

class SpaceUserRepositoryAdapter : SpaceUserRepositoryPort {

    override fun save(user: SpaceUser) {
        SpaceUsers.upsert {
            user.id?.let { entityId -> it[id] = entityId }
            it[spaceId] = user.spaceId
            it[userId] = user.userId.toString()
            it[role] = user.roleId
            it[isCreator] = user.isCreator
            it[createdAt] = user.createdAt
            it[updatedAt] = user.updatedAt
        }
    }

    override fun deleteUsersBySpaceId(spaceId: Long) {
        SpaceUsers.deleteWhere {
            this.spaceId eq spaceId
        }
    }

    override fun findAllRoleIdsBySpaceId(spaceId: Long): List<Long> {
        return SpaceUsers.select(column = SpaceUsers.role)
            .where(SpaceUsers.spaceId eq spaceId)
            .map { it[SpaceUsers.role].value }
    }

    override fun findBySpaceIdAndUserId(spaceId: Long, userId: UUID): SpaceUser? {
        return SpaceUsers.selectAll()
            .where { (SpaceUsers.spaceId eq spaceId) and (SpaceUsers.userId eq userId.toString()) }
            .singleOrNull()
            ?.toSpaceUser()
    }

    override fun deleteBySpaceIdAndUserId(spaceId: Long, userId: UUID) {
        SpaceUsers.deleteWhere {
            SpaceUsers.spaceId eq spaceId
            SpaceUsers.userId eq userId.toString()
        }
    }

    fun findAll(): List<SpaceUser> {
        return SpaceUsers.selectAll()
            .map { it.toSpaceUser() }
    }

    override fun findAllBySpaceIdAndUserIdsIn(spaceId: Long, userIds: List<UUID>): List<SpaceUser> {
        return SpaceUsers.selectAll()
            .where((SpaceUsers.spaceId eq spaceId) and (SpaceUsers.userId inList userIds.map { it.toString() }))
            .map { it.toSpaceUser() }
    }
}
