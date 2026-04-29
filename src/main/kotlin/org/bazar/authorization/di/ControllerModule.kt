package org.bazar.authorization.di

import org.bazar.authorization.controller.ActionController
import org.bazar.authorization.controller.SpaceUserController
import org.koin.dsl.module

fun controllerModule() = module {
    single { ActionController(get(), get()) }
    single { SpaceUserController(get(), get()) }
}