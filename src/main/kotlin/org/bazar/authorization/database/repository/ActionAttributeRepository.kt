package org.bazar.authorization.database.repository

import org.bazar.authorization.database.entity.ActionAttributeEntity
import org.bazar.authorization.database.tables.ActionAttributes
import org.bazar.authorization.utils.extensions.mapper.toActionAttribute
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ActionAttributeRepository {

    fun getAllAttributes(actionIds: List<Int>): List<ActionAttributeEntity> {
        return ActionAttributes.selectAll()
            .where { ActionAttributes.action inList actionIds }
            .map { it.toActionAttribute() }
    }

    suspend fun findByIds(ids: List<Int>) = suspendTransaction {
        ActionAttributes.selectAll()
            .where { ActionAttributes.id inList ids }
            .map { it.toActionAttribute() }
    }

}