package org.bazar.authorization.di

import org.bazar.authorization.config.AppConfig
import org.koin.dsl.module

fun appModule(config: AppConfig) = module {
    single { config }
}

