package org.bazar.authorization.infrastructure.di

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.CerbosClientBuilder
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.infrastructure.config.TestContainers
import org.koin.dsl.module

fun cerbosMockModule() = module {
    single<CerbosBlockingClient> {
        CerbosClientBuilder(TestContainers.cerbos.target)
            .withPlaintext()
            .buildBlockingClient()
    }
}