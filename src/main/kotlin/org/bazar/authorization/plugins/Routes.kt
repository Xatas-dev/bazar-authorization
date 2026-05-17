package org.bazar.authorization.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.bazar.authorization.controller.ActionController
import org.bazar.authorization.controller.RolesController
import org.bazar.authorization.controller.SpaceUsersController
import org.bazar.authorization.utils.ApiVersions
import org.koin.ktor.ext.inject

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