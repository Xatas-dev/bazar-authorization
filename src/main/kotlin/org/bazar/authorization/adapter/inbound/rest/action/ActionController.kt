package org.bazar.authorization.adapter.inbound.rest.action

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.bazar.authorization.application.action.port.`in`.GetActionsUseCase
import org.bazar.authorization.application.action.query.GetActionsQuery
import org.bazar.authorization.application.shared.AuthenticatedUserProvider

class ActionController(
    private val getActionsUseCase: GetActionsUseCase,
    private val authenticatedUserProvider: AuthenticatedUserProvider
) {

    fun Route.getAllActions() = get("/actions") {
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")
        val actions = getActionsUseCase.execute(GetActionsQuery(spaceId, authenticatedUserProvider.getUserId()))

        call.respond(actions.toResponse())
    }
}
