package org.bazar.authorization.application.spaceuser.port.out

import org.bazar.authorization.domain.spaceuser.SpaceUser
import java.util.UUID

interface SpaceUserRepositoryPort {
    suspend fun save(user: SpaceUser)

    suspend fun deleteUsersBySpaceId(spaceId: Long)

    suspend fun findAllRoleIdsBySpaceId(spaceId: Long): List<Long>

    suspend fun findBySpaceIdAndUserId(spaceId: Long, userId: UUID): SpaceUser?

    suspend fun deleteBySpaceIdAndUserId(spaceId: Long, userId: UUID)

    suspend fun findAllBySpaceIdAndUserIdsIn(spaceId: Long, userIds: List<UUID>): List<SpaceUser>
}
