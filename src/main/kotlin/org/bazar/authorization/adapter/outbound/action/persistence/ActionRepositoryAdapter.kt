package org.bazar.authorization.adapter.outbound.action.persistence

import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.domain.action.Action
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.selectAll

class ActionRepositoryAdapter : ActionRepositoryPort {

    override fun findByIds(ids: List<Int>): List<Action> {
        return Actions.selectAll()
            .where { Actions.id inList ids }
            .map { it.toAction() }
    }

    override fun findAll(): List<Action> {
        return Actions.selectAll()
            .map { it.toAction() }
    }
}
