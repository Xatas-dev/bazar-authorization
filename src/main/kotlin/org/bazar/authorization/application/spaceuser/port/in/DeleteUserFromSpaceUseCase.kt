package org.bazar.authorization.application.spaceuser.port.`in`

import org.bazar.authorization.application.spaceuser.command.DeleteUserFromSpaceCommand

interface DeleteUserFromSpaceUseCase {
    suspend fun execute(command: DeleteUserFromSpaceCommand)
}
