package org.bazar.authorization.plugins

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.bazar.authorization.controller.ActionController
import org.bazar.authorization.utils.ApiVersions
import org.bazar.authorization.controller.SpaceUserController
import org.bazar.authorization.utils.logger
import org.koin.ktor.ext.inject

fun Application.configureRoutes() {
    val actionController by inject<ActionController>()
    val spaceUserController by inject<SpaceUserController>()
    routing {
        authenticate("oauth") {
            route(ApiVersions.V1.path) {
                with(actionController) {
                    getAllActions()
                }
                with(spaceUserController) {
                    getRole()
                    createRole()
                    getRoleNames()
                }
            }
        }
    }
}