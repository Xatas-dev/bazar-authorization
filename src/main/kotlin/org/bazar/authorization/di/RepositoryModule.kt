package org.bazar.authorization.di

import org.bazar.authorization.database.repository.ActionRepository
import org.bazar.authorization.database.repository.RoleRepository
import org.bazar.authorization.database.repository.RolesActionsRepository
import org.bazar.authorization.database.repository.SpaceUserRepository
import org.koin.dsl.module

fun repositoryModule() = module {
    single { ActionRepository() }
    single { RoleRepository() }
    single { RolesActionsRepository() }
    single { SpaceUserRepository() }
}