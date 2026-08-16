package org.bazar.authorization.infrastructure.di

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.CerbosClientBuilder
import org.bazar.authorization.infrastructure.config.AppConfig
import org.koin.dsl.module

fun cerbosModule() = module {
    single<CerbosBlockingClient> {
        CerbosClientBuilder(get<AppConfig>().cerbos.url)
            .withPlaintext()
            .buildBlockingClient()
    }
}
