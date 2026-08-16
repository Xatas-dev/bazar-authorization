package org.bazar.authorization.application.spaceuser.port.`in`

import org.bazar.authorization.application.spaceuser.query.GetRoleNamesQuery
import org.bazar.authorization.application.spaceuser.UserRoleName

interface GetRoleNamesUseCase {
    suspend fun execute(query: GetRoleNamesQuery): List<UserRoleName>
}
