package org.bazar.authorization.application.role

import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.action.ActionAttribute
import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.application.role.RoleActionMapping

data class EnrichedRole(
    val role: Role,
    val actionMappings: List<RoleActionMapping>,
    val actions: List<Action>,
    val actionAttributes: List<ActionAttribute>
)
