package org.bazar.authorization.service

import org.bazar.authorization.database.entity.ActionAttributeEntity
import org.bazar.authorization.database.repository.ActionAttributeRepository
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ActionAttributeService (
    private val actionAttributeRepository: ActionAttributeRepository
) {

    suspend fun getAllAttributes(actionIds: List<Int>): List<ActionAttributeEntity> = suspendTransaction {
        actionAttributeRepository.getAllAttributes(actionIds)
    }

    suspend fun getAllAttributesByIds(attributeIds: List<Int>) = suspendTransaction {
        actionAttributeRepository.findByIds(attributeIds)
    }

}