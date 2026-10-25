package org.bazar.authorization.adapter.outbound.action.persistence

import org.bazar.authorization.domain.action.Action
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toAction(): Action = Action(
    id = this[Actions.id].value,
    code = this[Actions.code],
    name = this[Actions.name],
    resource = this[Actions.resource],
    dependsOn = this[Actions.dependsOn],
    resourceName = this[Actions.resourceName],
    createdAt = this[Actions.createdAt],
    updatedAt = this[Actions.updatedAt]
)
