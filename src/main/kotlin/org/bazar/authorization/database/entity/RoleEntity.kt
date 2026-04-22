package org.bazar.authorization.database.entity

import org.bazar.authorization.database.entity.enums.RoleScope
import java.time.Instant

data class RoleEntity(
    var name: String?,
    var spaceId: Long?,
    var scope: RoleScope,
    val id: Long? = null,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)
