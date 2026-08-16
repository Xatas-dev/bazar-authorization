package org.bazar.authorization.application.spaceuser.command

import java.util.UUID

data class AddUserToSpaceCommand(
    val spaceId: Long,
    val requesterId: UUID,
    val userId: UUID,
    val isCreator: Boolean
) {
    companion object {
        const val DEFAULT_ROLE_ID: Long = 1
    }
}
