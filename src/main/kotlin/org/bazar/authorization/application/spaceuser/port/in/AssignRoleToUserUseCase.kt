package org.bazar.authorization.application.spaceuser.port.`in`

import org.bazar.authorization.application.spaceuser.command.AssignRoleCommand

interface AssignRoleToUserUseCase {
    suspend fun execute(command: AssignRoleCommand)
}
