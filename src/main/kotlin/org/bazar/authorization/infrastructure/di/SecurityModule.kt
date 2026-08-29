package org.bazar.authorization.infrastructure.di

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.*
import org.bazar.authorization.adapter.inbound.grpc.JwtProvider
import org.bazar.authorization.adapter.outbound.authentication.KtorAuthContextProvider
import org.bazar.authorization.adapter.outbound.authentication.grpc.GrpcAuthContextProvider
import org.bazar.authorization.application.shared.AuthenticatedUserProvider
import org.bazar.authorization.application.shared.impl.CompositeAuthenticatedUserProvider
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
    single { KtorAuthContextProvider() }
    single { GrpcAuthContextProvider() }
    single<AuthenticatedUserProvider> {
        CompositeAuthenticatedUserProvider(
            listOf(get<KtorAuthContextProvider>(), get<GrpcAuthContextProvider>())
        )
    }
}
