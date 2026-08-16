package org.bazar.authorization.application.action

import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.action.ActionAttribute

data class ActionsWithAttributes(
    val actions: List<Action>,
    val attributes: List<ActionAttribute>
)
