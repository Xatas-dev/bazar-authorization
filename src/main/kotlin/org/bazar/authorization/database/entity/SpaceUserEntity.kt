package org.bazar.authorization.database.entity

import java.time.Instant
import java.util.*

data class SpaceUserEntity(
    val spaceId: Long,
    val userId: UUID,
    var roleId: Long,
    var isCreator: Boolean,
    val id: Long? = null,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)
