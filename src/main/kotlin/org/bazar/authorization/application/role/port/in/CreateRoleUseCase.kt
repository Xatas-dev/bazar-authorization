package org.bazar.authorization.application.role.port.`in`

import org.bazar.authorization.application.role.command.CreateRoleCommand
import org.bazar.authorization.application.role.EnrichedRole

interface CreateRoleUseCase {
    suspend fun execute(command: CreateRoleCommand): EnrichedRole
}
