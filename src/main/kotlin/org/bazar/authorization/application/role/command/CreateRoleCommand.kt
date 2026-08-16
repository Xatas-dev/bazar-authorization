package org.bazar.authorization.application.role.command

import java.util.UUID

data class CreateRoleCommand(
    val name: String,
    val spaceId: Long,
    val isVisible: Boolean,
    val createdBy: UUID,
    val actions: List<RoleActionToGrant>
)

data class RoleActionToGrant(
    val actionId: Int,
    val attributes: List<RoleAttributeToGrant> = emptyList()
)

data class RoleAttributeToGrant(
    val attributeId: Int,
    val value: String
)
