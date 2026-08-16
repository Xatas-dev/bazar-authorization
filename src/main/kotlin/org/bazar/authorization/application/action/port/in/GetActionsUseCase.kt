package org.bazar.authorization.application.action.port.`in`

import org.bazar.authorization.application.action.query.GetActionsQuery
import org.bazar.authorization.application.action.ActionsWithAttributes

interface GetActionsUseCase {
    suspend fun execute(query: GetActionsQuery): ActionsWithAttributes
}
