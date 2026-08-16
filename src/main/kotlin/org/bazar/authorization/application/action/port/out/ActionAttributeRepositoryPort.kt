package org.bazar.authorization.application.action.port.out

import org.bazar.authorization.domain.action.ActionAttribute

interface ActionAttributeRepositoryPort {
    fun findAllByActionIds(actionIds: List<Int>): List<ActionAttribute>

    fun findByIds(ids: List<Int>): List<ActionAttribute>
}
