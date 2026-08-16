package org.bazar.authorization.infrastructure.di

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.*
import org.bazar.authorization.adapter.inbound.grpc.JwtProvider
import org.bazar.authorization.infrastructure.config.AppConfig
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

fun securityModule() = module {
    single {
        JwkProviderBuilder(URLBuilder(get<AppConfig>().auth.jwkUrl).build().toURI().toURL()).cached(
            10,
            24,
            TimeUnit.HOURS
        ).build()
    }
    single { JwtProvider(get<AppConfig>().auth.issuer, get()) }
}
