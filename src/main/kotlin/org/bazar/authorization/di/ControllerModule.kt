package org.bazar.authorization.di

import org.bazar.authorization.controller.ActionController
import org.bazar.authorization.controller.RolesController
import org.bazar.authorization.controller.SpaceUsersController
import org.koin.dsl.module

fun controllerModule() = module {
    single { ActionController(get(), get()) }
    single { SpaceUsersController(get(), get()) }
    single { RolesController(get(), get()) }
}