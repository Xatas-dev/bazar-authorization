package org.bazar.authorization.adapter.outbound.spaceuser.persistence

import org.bazar.authorization.domain.spaceuser.SpaceUser
import org.bazar.authorization.infrastructure.util.extension.toUuid
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toSpaceUser(): SpaceUser = SpaceUser(
    id = this[SpaceUsers.id].value,
    spaceId = this[SpaceUsers.spaceId],
    userId = this[SpaceUsers.userId].toUuid(),
    roleId = this[SpaceUsers.role].value,
    isCreator = this[SpaceUsers.isCreator],
    createdAt = this[SpaceUsers.createdAt],
    updatedAt = this[SpaceUsers.updatedAt]
)
