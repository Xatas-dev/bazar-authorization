package org.bazar.authorization.application.action.port.`in`

import org.bazar.authorization.application.action.query.GetActionsQuery
import org.bazar.authorization.domain.action.Action

interface GetActionsUseCase {
    suspend fun execute(query: GetActionsQuery): List<Action>
}
