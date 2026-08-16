package org.bazar.authorization.application.spaceuser.port.`in`

import org.bazar.authorization.application.spaceuser.command.DeleteSpaceCommand

interface DeleteSpaceUseCase {
    suspend fun execute(command: DeleteSpaceCommand)
}
