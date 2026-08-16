package org.bazar.authorization.infrastructure.plugins

import io.ktor.server.application.*
import org.bazar.authorization.adapter.inbound.grpc.GrpcServerImpl
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
