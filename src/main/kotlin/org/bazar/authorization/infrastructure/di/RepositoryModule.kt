package org.bazar.authorization.infrastructure.di

import org.bazar.authorization.adapter.outbound.action.persistence.ActionAttributeRepositoryAdapter
import org.bazar.authorization.adapter.outbound.action.persistence.ActionRepositoryAdapter
import org.bazar.authorization.adapter.outbound.role.persistence.RoleRepositoryAdapter
import org.bazar.authorization.adapter.outbound.role.persistence.RolesActionsRepositoryAdapter
import org.bazar.authorization.adapter.outbound.spaceuser.persistence.SpaceUserRepositoryAdapter
import org.bazar.authorization.application.action.port.out.ActionAttributeRepositoryPort
import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.koin.dsl.module

fun repositoryModule() = module {
    single<RoleRepositoryPort> { RoleRepositoryAdapter() }
    single<RolesActionsRepositoryPort> { RolesActionsRepositoryAdapter() }
    single<SpaceUserRepositoryPort> { SpaceUserRepositoryAdapter() }
    single<ActionRepositoryPort> { ActionRepositoryAdapter() }
    single<ActionAttributeRepositoryPort> { ActionAttributeRepositoryAdapter() }
}
