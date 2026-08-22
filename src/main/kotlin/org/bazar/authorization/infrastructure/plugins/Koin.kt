package org.bazar.authorization.infrastructure.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.bazar.authorization.infrastructure.config.AppConfig
import org.bazar.authorization.infrastructure.di.appModule
import org.bazar.authorization.infrastructure.di.cerbosModule
import org.bazar.authorization.infrastructure.di.controllerModule
import org.bazar.authorization.infrastructure.di.databaseModule
import org.bazar.authorization.infrastructure.di.grpcModule
import org.bazar.authorization.infrastructure.di.outboundModule
import org.bazar.authorization.infrastructure.di.repositoryModule
import org.bazar.authorization.infrastructure.di.securityModule
import org.bazar.authorization.infrastructure.di.useCaseModule
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin(appConfig: AppConfig) {
    install(Koin) {
        slf4jLogger()
        modules(
            appModule(appConfig),
            securityModule(),
            grpcModule(),
            repositoryModule(),
            useCaseModule(),
            cerbosModule(),
            databaseModule(),
            controllerModule(),
            outboundModule()
        )
    }
}
