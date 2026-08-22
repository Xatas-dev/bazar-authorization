package org.bazar.authorization.infrastructure.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.auth.principal
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.withContext
import org.bazar.authorization.adapter.inbound.rest.action.ActionController
import org.bazar.authorization.adapter.inbound.rest.role.RolesController
import org.bazar.authorization.adapter.inbound.rest.spaceuser.SpaceUsersController
import org.bazar.authorization.infrastructure.util.ApiVersions
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureRoutes() {
    val actionController by inject<ActionController>()
    val spaceUsersController by inject<SpaceUsersController>()
    val roleController by inject<RolesController>()
    routing {
        authenticate("oauth") {
            route(ApiVersions.V1.path) {
                with(actionController) {
                    getAllActions()
                }
                with(roleController) {
                    getRoles()
                    getSingleEnrichedRole()
                    createRole()
                    putRole()
                }
                with(spaceUsersController) {
                    patchSpaceUsersRole()
                    getRoleNames()
                }
            }
        }
    }
}
