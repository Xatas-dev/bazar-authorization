package org.bazar.authorization.plugins

import io.grpc.BindableService
import io.grpc.ServerInterceptor
import io.ktor.server.application.*
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.grpc.GrpcServerImpl
import org.bazar.authorization.grpc.SpaceAdminAuthorizationService
import org.bazar.authorization.grpc.SpaceAuthorizationService
import org.bazar.authorization.utils.Profile
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject

fun Application.configureGrpcServer() {
    val appConfig by inject<AppConfig>()
    val authService = getKoin().get<SpaceAuthorizationService>()
    val adminAuthService = getKoin().get<SpaceAdminAuthorizationService>()
    val interceptors = getKoin().getAll<ServerInterceptor>()
    val server = GrpcServerImpl(
        port = appConfig.grpc.port,
        interceptors,
        services = listOf(authService, adminAuthService)
    )

    monitor.subscribe(ApplicationStarted) {
        server.start()
    }
    monitor.subscribe(ApplicationStopped) {
        server.stop()
    }
}