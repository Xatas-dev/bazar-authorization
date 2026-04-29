package org.bazar.authorization.service.api

import org.bazar.authorization.model.rest.response.GetActionsResponse
import org.bazar.authorization.service.ActionAttributeService
import org.bazar.authorization.service.ActionService
import org.bazar.authorization.utils.buildGetActionsResponse

class ActionApiService(
    private val actionService: ActionService,
    private val actionAttributeService: ActionAttributeService
) {

    suspend fun getActionsWithAttributes(): GetActionsResponse {
        val actions = actionService.getAllActions()
        val attributes = actionAttributeService.getAllAttributes(actions.map { it.id })

        return buildGetActionsResponse(actions, attributes)
    }

}