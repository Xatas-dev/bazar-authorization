package org.bazar.authorization.plugins

import io.ktor.openapi.OpenApiDoc
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.plus
import io.ktor.server.routing.path
import io.ktor.server.routing.routing
import io.ktor.server.routing.routingRoot

fun Application.configureOpenApiGenerator() {
    routing {
        get("/openapi") {
            val doc = OpenApiDoc(info = OpenApiInfo("My API", "1.0")) + call.application.routingRoot.descendants()
                .filter { it.path.startsWith("/api")}
            call.respond(doc)
        }
    }
}