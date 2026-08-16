package org.bazar.authorization.application.spaceuser.command

import java.util.UUID

data class DeleteUserFromSpaceCommand(
    val spaceId: Long,
    val requesterId: UUID,
    val userId: UUID,
    val isCreator: Boolean
)
