package org.bazar.authorization.adapter.outbound.action.persistence

import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.action.ActionAttribute
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toAction(): Action = Action(
    id = this[Actions.id].value,
    code = this[Actions.code],
    name = this[Actions.name],
    resource = this[Actions.resource],
    resourceName = this[Actions.resourceName],
    createdAt = this[Actions.createdAt],
    updatedAt = this[Actions.updatedAt]
)

fun ResultRow.toActionAttribute(): ActionAttribute = ActionAttribute(
    id = this[ActionAttributes.id].value,
    actionId = this[ActionAttributes.action].value,
    name = this[ActionAttributes.name],
    displayName = this[ActionAttributes.displayName],
    valueType = this[ActionAttributes.valueType],
    createdAt = this[ActionAttributes.createdAt],
    updatedAt = this[ActionAttributes.updatedAt]
)
