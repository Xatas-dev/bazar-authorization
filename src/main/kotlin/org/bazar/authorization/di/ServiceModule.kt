package org.bazar.authorization.di

import org.bazar.authorization.service.*
import org.bazar.authorization.service.api.ActionApiService
import org.bazar.authorization.service.api.RolesApiService
import org.bazar.authorization.service.api.SpaceUserApiService
import org.bazar.authorization.service.attribute_extractor.AttributeExtractor
import org.bazar.authorization.service.attribute_extractor.DefaultUserPrincipalAttributeExtractor
import org.bazar.authorization.service.attribute_extractor.RolesResourceAttributeExtractor
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun serviceModule() = module {
    single { CerbosAccessService(get()) }
    single { ActionService(get()) }
    single { AuthorizationService(get(), get(), getAll(AttributeExtractor::class)) }
    single { RoleService(get(), get()) }
    single { SpaceUserService(get()) }
    single { ActionAttributeService(get()) }
    single { ActionApiService(get(), get()) }
    single { SpaceUserApiService(get()) }
    single { RolesApiService(get(), get(), get(), get()) }

    single<AttributeExtractor>(named("defaultUserPrincipalAttributeExtractor")) {
        DefaultUserPrincipalAttributeExtractor(
            get(),
            get()
        )
    }
    single<AttributeExtractor>(named("rolesResourceAttributeExtractor")) { RolesResourceAttributeExtractor(get()) }


}