package org.bazar.authorization.application.role.port.`in`

import org.bazar.authorization.application.role.command.UpdateRoleCommand
import org.bazar.authorization.application.role.EnrichedRole

interface UpdateRoleUseCase {
    suspend fun execute(command: UpdateRoleCommand): EnrichedRole
}
