package org.bazar.authorization.application.spaceuser

import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.domain.spaceuser.SpaceUser

data class UserRoleName(
    val user: SpaceUser,
    val role: Role
)
