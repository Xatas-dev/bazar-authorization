package org.bazar.authorization.di

import org.bazar.authorization.service.*
import org.koin.dsl.module

fun serviceModule() = module {
    single { CerbosAccessService(get()) }
    single { ActionService(get()) }
    single { AuthorizationService(get(), get(), get(), get()) }
    single { RoleService(get(), get()) }
    single { SpaceUserService(get()) }
}