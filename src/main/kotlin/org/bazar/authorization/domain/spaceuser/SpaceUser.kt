package org.bazar.authorization.domain.spaceuser

import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException
import java.time.Instant
import java.util.UUID

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
        if (newRoleId <= 0) {
            throw DomainException(DomainErrors.ILLEGAL_ARGUMENT, "Role id must be positive: $newRoleId")
        }
        return copy(roleId = newRoleId, updatedAt = Instant.now())
    }

    companion object {
        const val DEFAULT_ROLE_ID: Long = 1

        fun create(spaceId: Long, userId: UUID, isCreator: Boolean, roleId: Long = DEFAULT_ROLE_ID): SpaceUser {
            if (spaceId <= 0) {
                throw DomainException(DomainErrors.ILLEGAL_ARGUMENT, "Space id must be positive: $spaceId")
            }
            if (roleId <= 0) {
                throw DomainException(DomainErrors.ILLEGAL_ARGUMENT, "Role id must be positive: $roleId")
            }
            return SpaceUser(spaceId, userId, roleId, isCreator)
        }
    }
}
