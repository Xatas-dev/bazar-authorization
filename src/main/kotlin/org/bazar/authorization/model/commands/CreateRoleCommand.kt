package org.bazar.authorization.model.commands

import org.bazar.authorization.database.entity.enums.RoleScope
import java.util.UUID

data class CreateRoleCommand(
    val name: String,
    val spaceId: Long?,
    val scope: RoleScope,
    val isVisible: Boolean,
    val createdBy: UUID,
    val actionIdToAttributes: Map<Int, Map<String, String>?>
)
