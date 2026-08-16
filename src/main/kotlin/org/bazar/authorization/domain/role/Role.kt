package org.bazar.authorization.domain.role

import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException
import java.time.Instant
import java.util.UUID

data class Role(
    val scope: RoleScope,
    val name: String,
    val isVisible: Boolean,
    val createdBy: UUID?,
    val spaceId: Long? = null,
    val id: Long? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {

    fun update(name: String, isVisible: Boolean): Role =
        copy(name = validatedName(name), isVisible = isVisible, updatedAt = Instant.now())

    companion object {
        fun createSpaceScoped(
            name: String,
            isVisible: Boolean,
            createdBy: UUID,
            spaceId: Long
        ): Role {
            if (spaceId <= 0) {
                throw DomainException(DomainErrors.ILLEGAL_ARGUMENT, "Space id must be positive: $spaceId")
            }
            return Role(RoleScope.SPACE, validatedName(name), isVisible, createdBy, spaceId)
        }

        private fun validatedName(name: String): String {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                throw DomainException(DomainErrors.ILLEGAL_ARGUMENT, "Role name must not be blank")
            }
            if (trimmed.length > 255) {
                throw DomainException(DomainErrors.ILLEGAL_ARGUMENT, "Role name must not exceed 255 characters")
            }
            return trimmed
        }
    }
}
