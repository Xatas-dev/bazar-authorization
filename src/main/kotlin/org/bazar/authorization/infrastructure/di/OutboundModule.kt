package org.bazar.authorization.infrastructure.di

import org.bazar.authorization.adapter.outbound.http.BazarSpaceHttpClient
import org.bazar.authorization.adapter.outbound.spaceuser.http.BazarSpaceUserProvider
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserProviderPort
import org.koin.dsl.module

fun outboundModule() = module {
    single { BazarSpaceHttpClient(get(), get(), get()) }
    single<SpaceUserProviderPort> { BazarSpaceUserProvider(get()) }
}