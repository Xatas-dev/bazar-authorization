package org.bazar.authorization.di

import org.bazar.authorization.service.*
import org.bazar.authorization.service.api.ActionApiService
import org.bazar.authorization.service.api.SpaceUserApiService
import org.koin.dsl.module

fun serviceModule() = module {
    single { CerbosAccessService(get()) }
    single { ActionService(get()) }
    single { AuthorizationService(get(), get(), get(), get()) }
    single { RoleService(get(), get()) }
    single { SpaceUserService(get()) }
    single { ActionAttributeService(get()) }
    single { ActionApiService(get(), get()) }
    single { SpaceUserApiService(get(), get(), get(), get()) }
}