package org.bazar.authorization.application.role.command

import java.util.UUID

data class CreateRoleCommand(
    val name: String,
    val spaceId: Long,
    val isVisible: Boolean,
    val createdBy: UUID,
    val actions: List<RoleActionToGrant>
)
//TODO: change actionId to action code + resource for easier mapping in code
data class RoleActionToGrant(
    val actionId: Int
)
