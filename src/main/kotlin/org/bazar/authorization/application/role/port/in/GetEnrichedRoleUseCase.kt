package org.bazar.authorization.application.role.port.`in`

import org.bazar.authorization.application.role.query.GetRoleQuery
import org.bazar.authorization.application.role.EnrichedRole

interface GetEnrichedRoleUseCase {
    suspend fun execute(query: GetRoleQuery): EnrichedRole
}
