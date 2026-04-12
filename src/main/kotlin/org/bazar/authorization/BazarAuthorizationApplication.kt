package org.bazar.authorization

import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import org.bazar.authorization.plugins.*
import org.bazar.authorization.plugins.database.configureDatabase

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val appConfig = getAppConfig()
    configureKoin(appConfig)
    configureDatabase()
    configureGrpcServer()
    configureContentNegotiations()
//    configureMonitoring()
    configureSecurity()
    configureRoutes()
    configureStatusPages()
    configureOpenApiGenerator()
}