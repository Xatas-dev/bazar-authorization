package org.bazar.authorization.di

import org.bazar.authorization.database.repository.UserSpaceRoleRepository
import org.koin.dsl.module

fun repositoryModule() = module {
    single { UserSpaceRoleRepository() }
}