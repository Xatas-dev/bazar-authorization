package org.bazar.authorization.di

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.*
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.utils.JwtProvider
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
    single { JwtProvider(get<AppConfig>().auth, get()) }
}