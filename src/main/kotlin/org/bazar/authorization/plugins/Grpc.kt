package org.bazar.authorization.plugins

import io.ktor.server.application.*
import org.bazar.authorization.grpc.GrpcServerImpl
import org.koin.ktor.ext.inject

fun Application.configureGrpcServer() {
    val server by inject<GrpcServerImpl>()

    monitor.subscribe(ApplicationStarted) {
        server.start()
    }
    monitor.subscribe(ApplicationStopping) {
        server.stop()
    }
}