package org.bazar.authorization.di

import org.bazar.authorization.service.CerbosAccessService
import org.bazar.authorization.service.UserSpaceRoleService
import org.koin.dsl.module

fun serviceModule() = module {
    single { UserSpaceRoleService(get()) }
    single { CerbosAccessService(get(), get()) }
}