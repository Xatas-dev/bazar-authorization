package org.bazar.authorization.adapter.inbound.rest.action

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.util.getOrFail
import org.bazar.authorization.adapter.inbound.rest.getAuthenticatedUserId
import org.bazar.authorization.application.action.port.`in`.GetActionsUseCase
import org.bazar.authorization.application.action.query.GetActionsQuery

class ActionController(
    private val getActionsUseCase: GetActionsUseCase
) {

    fun Route.getAllActions() = get("/actions") {
        val spaceId = call.queryParameters.getOrFail<Long>("spaceId")

        val actions = getActionsUseCase.execute(GetActionsQuery(spaceId, call.getAuthenticatedUserId()))

        call.respond(actions.toResponse())
    }
}
