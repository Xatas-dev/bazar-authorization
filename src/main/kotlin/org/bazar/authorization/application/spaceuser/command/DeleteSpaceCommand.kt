package org.bazar.authorization.application.spaceuser.command

import java.util.UUID

data class DeleteSpaceCommand(
    val spaceId: Long,
    val requesterId: UUID
)
