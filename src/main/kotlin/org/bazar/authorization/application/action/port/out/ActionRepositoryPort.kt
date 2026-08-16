package org.bazar.authorization.application.action.port.out

import org.bazar.authorization.domain.action.Action

interface ActionRepositoryPort {
    fun findByIds(ids: List<Int>): List<Action>

    fun findAll(): List<Action>
}
