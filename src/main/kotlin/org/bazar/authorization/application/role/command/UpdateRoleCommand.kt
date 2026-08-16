package org.bazar.authorization.application.role.command

import java.util.UUID

data class UpdateRoleCommand(
    val roleId: Long,
    val spaceId: Long,
    val requesterId: UUID,
    val name: String,
    val isVisible: Boolean,
    val actions: List<RoleActionToGrant>
)
