package org.bazar.authorization.application.role

import java.time.Instant

data class RoleActionMapping(
    val roleId: Long,
    val actionId: Int,
    val assignedAttributes: Map<String, String>? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
