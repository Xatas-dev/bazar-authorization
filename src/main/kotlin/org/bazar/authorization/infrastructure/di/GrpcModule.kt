package org.bazar.authorization.infrastructure.di

import io.grpc.BindableService
import io.grpc.ServerInterceptor
import org.bazar.authorization.adapter.inbound.grpc.GrpcAuthInterceptor
import org.bazar.authorization.adapter.inbound.grpc.GrpcExceptionHandler
import org.bazar.authorization.adapter.inbound.grpc.GrpcExceptionTranslatorInterceptor
import org.bazar.authorization.adapter.inbound.grpc.GrpcServerImpl
import org.bazar.authorization.adapter.inbound.grpc.MockGrpcAuthInterceptor
import org.bazar.authorization.adapter.inbound.grpc.spaceuser.SpaceAdminAuthorizationService
import org.bazar.authorization.adapter.inbound.grpc.authz.SpaceAuthorizationService
import org.bazar.authorization.infrastructure.config.AppConfig
import org.bazar.authorization.infrastructure.config.Profile
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun grpcModule() = module {
    single<BindableService>(named("authGrpcService")) { SpaceAuthorizationService(get()) }
    single<BindableService>(named("authAdminGrpcService")) {
        SpaceAdminAuthorizationService(get(), get(), get())
    }
    single<ServerInterceptor>(named("auth")) {
        val appConfig: AppConfig = get()
        if (appConfig.profile == Profile.LOCAL || appConfig.profile == Profile.TEST) {
            MockGrpcAuthInterceptor()
        } else {
            GrpcAuthInterceptor(get())
        }
    }
    single { GrpcExceptionHandler() }
    single<ServerInterceptor>(named("exceptionHandler")) { GrpcExceptionTranslatorInterceptor(get()) }

    single {
        GrpcServerImpl(
            get<AppConfig>().grpc.port,
            getAll(),
            getAll<BindableService>()
        )
    }
}
