package org.bazar.authorization.infrastructure.di

import org.bazar.authorization.adapter.inbound.rest.action.ActionController
import org.bazar.authorization.adapter.inbound.rest.role.RolesController
import org.bazar.authorization.adapter.inbound.rest.spaceuser.SpaceUsersController
import org.koin.dsl.module

fun controllerModule() = module {
    single { ActionController(get(), get()) }
    single { SpaceUsersController(get(), get(), get()) }
    single { RolesController(get(), get(), get(), get(), get()) }
}
