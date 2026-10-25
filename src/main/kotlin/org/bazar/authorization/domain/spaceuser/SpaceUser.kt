package org.bazar.authorization.domain.spaceuser

import org.bazar.authorization.domain.exception.InvalidArgumentException
import java.time.Instant
import java.util.*

data class SpaceUser(
    val spaceId: Long,
    val userId: UUID,
    val roleId: Long,
    val isCreator: Boolean,
    val id: Long? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {

    fun withRole(newRoleId: Long): SpaceUser {
        requirePositive("roleId", newRoleId)
        return copy(roleId = newRoleId, updatedAt = Instant.now())
    }

    companion object {
        const val DEFAULT_ROLE_ID: Long = 1

        fun create(spaceId: Long, userId: UUID, isCreator: Boolean, roleId: Long = DEFAULT_ROLE_ID): SpaceUser {
            requirePositive("spaceId", spaceId)
            requirePositive("roleId", roleId)
            return SpaceUser(spaceId, userId, roleId, isCreator)
        }

        private fun requirePositive(field: String, value: Long) {
            if (value <= 0) throw InvalidArgumentException(field, "must be positive, got $value")
        }
    }
}
