package org.bazar.authorization.application.spaceuser.port.`in`

import org.bazar.authorization.application.spaceuser.command.AddUserToSpaceCommand

interface AddUserToSpaceUseCase {
    suspend fun execute(command: AddUserToSpaceCommand)
}
