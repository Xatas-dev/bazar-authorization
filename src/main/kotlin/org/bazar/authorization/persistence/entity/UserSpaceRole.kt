package org.bazar.authorization.persistence.entity

import org.bazar.authorization.persistence.entity.enums.Role
import java.time.Instant
import java.util.*

data class UserSpaceRole(
    val spaceId: Long,
    val userId: UUID,
    val role: Role,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)