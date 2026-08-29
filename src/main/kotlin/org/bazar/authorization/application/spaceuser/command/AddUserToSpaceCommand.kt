package org.bazar.authorization.application.spaceuser.command

import java.util.*

data class AddUserToSpaceCommand(
    val spaceId: Long,
    val requesterId: UUID,
    val userId: UUID,
    val isCreator: Boolean
)
