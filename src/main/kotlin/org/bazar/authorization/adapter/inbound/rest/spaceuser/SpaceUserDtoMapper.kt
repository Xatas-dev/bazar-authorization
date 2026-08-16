package org.bazar.authorization.adapter.inbound.rest.spaceuser

import org.bazar.authorization.application.spaceuser.UserRoleName
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetRoleNameDto

fun UserRoleName.toGetRoleNameDto() = GetRoleNameDto(
    id = role.id!!,
    name = role.name,
    userId = user.userId.toString(),
    isVisible = role.isVisible,
    isCreator = user.isCreator
)
