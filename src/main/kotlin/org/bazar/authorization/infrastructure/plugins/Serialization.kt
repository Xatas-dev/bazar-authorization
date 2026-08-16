package org.bazar.authorization.infrastructure.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

fun Application.configureContentNegotiations() {
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
        })
    }
}
