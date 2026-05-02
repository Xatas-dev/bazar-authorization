package org.bazar.authorization

import io.ktor.server.application.*
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
    configureMonitoring()
    configureSecurity()
    configureRoutes()
    configureStatusPages()
    configureOpenApiGenerator()
}