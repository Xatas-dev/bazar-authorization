package org.bazar.authorization.plugins

import io.ktor.server.application.*
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.di.*
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
            serviceModule(),
            cerbosModule()
        )
    }
}