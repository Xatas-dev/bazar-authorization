package org.bazar.authorization.infrastructure.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.bazar.authorization.infrastructure.config.AppConfig
import org.koin.dsl.module
import org.koin.dsl.onClose

fun appModule(config: AppConfig) = module {
    single { config }
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }.onClose { it?.close() }
}
