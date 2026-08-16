package org.bazar.authorization.application.role.port.`in`

import org.bazar.authorization.application.role.query.GetRolesQuery
import org.bazar.authorization.domain.role.Role

interface GetRolesInSpaceUseCase {
    suspend fun execute(query: GetRolesQuery): List<Role>
}
