package org.bazar.authorization.adapter.outbound.spaceuser.persistence

import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.spaceuser.SpaceUser
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.UUID

class SpaceUserRepositoryAdapter : SpaceUserRepositoryPort {

    override suspend fun save(user: SpaceUser): Unit = suspendTransaction {
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

    override suspend fun deleteUsersBySpaceId(spaceId: Long): Unit = suspendTransaction {
        SpaceUsers.deleteWhere {
            this.spaceId eq spaceId
        }
    }

    override suspend fun findAllRoleIdsBySpaceId(spaceId: Long): List<Long> = suspendTransaction{
        SpaceUsers.select(column = SpaceUsers.role)
            .where(SpaceUsers.spaceId eq spaceId)
            .map { it[SpaceUsers.role].value }
    }

    override suspend fun findBySpaceIdAndUserId(spaceId: Long, userId: UUID): SpaceUser? = suspendTransaction{
        SpaceUsers.selectAll()
            .where { (SpaceUsers.spaceId eq spaceId) and (SpaceUsers.userId eq userId.toString()) }
            .singleOrNull()
            ?.toSpaceUser()
    }

    override suspend fun deleteBySpaceIdAndUserId(spaceId: Long, userId: UUID): Unit = suspendTransaction{
        SpaceUsers.deleteWhere {
            SpaceUsers.spaceId eq spaceId
            SpaceUsers.userId eq userId.toString()
        }
    }

    override suspend fun findAllBySpaceIdAndUserIdsIn(spaceId: Long, userIds: List<UUID>): List<SpaceUser> = suspendTransaction {
        SpaceUsers.selectAll()
            .where((SpaceUsers.spaceId eq spaceId) and (SpaceUsers.userId inList userIds.map { it.toString() }))
            .map { it.toSpaceUser() }
    }
}
