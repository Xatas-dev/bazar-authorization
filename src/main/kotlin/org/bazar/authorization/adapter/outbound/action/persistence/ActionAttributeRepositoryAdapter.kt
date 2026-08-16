package org.bazar.authorization.adapter.outbound.action.persistence

import org.bazar.authorization.application.action.port.out.ActionAttributeRepositoryPort
import org.bazar.authorization.domain.action.ActionAttribute
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.selectAll

class ActionAttributeRepositoryAdapter : ActionAttributeRepositoryPort {

    override fun findAllByActionIds(actionIds: List<Int>): List<ActionAttribute> {
        return ActionAttributes.selectAll()
            .where { ActionAttributes.action inList actionIds }
            .map { it.toActionAttribute() }
    }

    override fun findByIds(ids: List<Int>): List<ActionAttribute> {
        return ActionAttributes.selectAll()
            .where { ActionAttributes.id inList ids }
            .map { it.toActionAttribute() }
    }
}
