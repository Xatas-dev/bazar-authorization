package org.bazar.authorization.application.spaceuser.port.out

import org.bazar.authorization.domain.spaceuser.SpaceUser
import java.util.UUID

interface SpaceUserRepositoryPort {
    fun save(user: SpaceUser)

    fun deleteUsersBySpaceId(spaceId: Long)

    fun findAllRoleIdsBySpaceId(spaceId: Long): List<Long>

    fun findBySpaceIdAndUserId(spaceId: Long, userId: UUID): SpaceUser?

    fun deleteBySpaceIdAndUserId(spaceId: Long, userId: UUID)

    fun findAllBySpaceIdAndUserIdsIn(spaceId: Long, userIds: List<UUID>): List<SpaceUser>
}
