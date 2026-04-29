package org.bazar.authorization.database.repository

import org.bazar.authorization.database.entity.ActionEntity
import org.bazar.authorization.database.tables.Actions
import org.bazar.authorization.utils.extensions.toActionEntity
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ActionRepository {

    fun findByCodeAndResource(actionCode: String, resource: String): ActionEntity? {
        return Actions.selectAll()
            .where {
                (Actions.code eq actionCode) and
                        (Actions.resource eq resource)
            }
            .singleOrNull()?.toActionEntity()
    }

    fun getAllActions(): List<ActionEntity> {
        return Actions.selectAll()
            .map { it.toActionEntity() }
    }

    suspend fun findAllByIds(ids: List<Int>) = suspendTransaction {
        Actions.selectAll()
            .where { Actions.id inList ids }
            .map { it.toActionEntity() }
    }

}