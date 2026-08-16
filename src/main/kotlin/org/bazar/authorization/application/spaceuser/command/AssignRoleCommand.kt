package org.bazar.authorization.application.spaceuser.command

import java.util.UUID

data class AssignRoleCommand(
    val spaceId: Long,
    val requesterId: UUID,
    val targetUserId: UUID,
    val roleId: Long
)
