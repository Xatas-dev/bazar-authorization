package org.bazar.authorization.domain.role

import java.time.Instant

data class RoleActionMapping(
    val roleId: Long,
    val actionId: Int,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
