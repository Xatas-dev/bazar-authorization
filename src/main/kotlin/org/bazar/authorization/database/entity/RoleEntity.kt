package org.bazar.authorization.database.entity

import org.bazar.authorization.database.entity.enums.RoleScope
import java.time.Instant
import java.util.UUID

data class RoleEntity(
    var scope: RoleScope,
    var name: String,
    var isVisible: Boolean,
    var createdBy: UUID?,
    var spaceId: Long? = null,
    val id: Long? = null,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)
