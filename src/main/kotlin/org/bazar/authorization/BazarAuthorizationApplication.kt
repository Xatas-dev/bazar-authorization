package org.bazar.authorization

import io.ktor.server.application.*
import org.bazar.authorization.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val appConfig = applyConfigurations()
    configureKoin(appConfig)
    configureDatabase()
    configureGrpcServer()
    configureContentNegotiations()
    configureMonitoring()
}
