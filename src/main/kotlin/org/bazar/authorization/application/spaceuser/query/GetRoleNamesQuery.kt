package org.bazar.authorization.application.spaceuser.query

import java.util.UUID

data class GetRoleNamesQuery(
    val spaceId: Long,
    val requesterId: UUID,
    val userIds: List<UUID>
)
