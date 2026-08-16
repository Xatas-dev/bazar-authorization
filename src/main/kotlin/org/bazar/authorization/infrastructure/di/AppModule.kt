package org.bazar.authorization.infrastructure.di

import org.bazar.authorization.infrastructure.config.AppConfig
import org.koin.dsl.module

fun appModule(config: AppConfig) = module {
    single { config }
}
