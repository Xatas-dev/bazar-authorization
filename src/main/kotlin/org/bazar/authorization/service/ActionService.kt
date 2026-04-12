package org.bazar.authorization.service

import org.bazar.authorization.database.entity.ActionEntity
import org.bazar.authorization.database.repository.ActionRepository
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ActionService(
    private val actionRepository: ActionRepository
) {

    suspend fun getActionByNameAndResourceOrThrow(actionCode: String, resource: String): ActionEntity =
        suspendTransaction {
            actionRepository.findByCodeAndResource(actionCode, resource)
                ?: throw ApiException(ApiExceptions.NO_SUCH_ACTION, actionCode)
        }

    suspend fun findAllByIds(ids: List<Int>) = suspendTransaction {
        actionRepository.findAllByIds(ids)
    }

    suspend fun getAllActions() = suspendTransaction {
        actionRepository.getAllActions()
    }

}