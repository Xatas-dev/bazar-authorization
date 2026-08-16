package org.bazar.authorization.infrastructure.di

import org.bazar.authorization.adapter.outbound.authz.AuthorizerAdapter
import org.bazar.authorization.adapter.outbound.cerbos.CerbosAccessChecker
import org.bazar.authorization.application.action.port.`in`.GetActionsUseCase
import org.bazar.authorization.application.action.port.`in`.impl.GetActionsUseCaseImpl
import org.bazar.authorization.application.authz.port.`in`.AuthorizeUseCase
import org.bazar.authorization.application.authz.port.`in`.impl.AuthorizeUseCaseImpl
import org.bazar.authorization.application.role.port.`in`.CreateRoleUseCase
import org.bazar.authorization.application.role.port.`in`.GetEnrichedRoleUseCase
import org.bazar.authorization.application.role.port.`in`.GetRolesInSpaceUseCase
import org.bazar.authorization.application.role.port.`in`.UpdateRoleUseCase
import org.bazar.authorization.application.role.port.`in`.impl.CreateRoleUseCaseImpl
import org.bazar.authorization.application.role.port.`in`.impl.GetEnrichedRoleUseCaseImpl
import org.bazar.authorization.application.role.port.`in`.impl.GetRolesInSpaceUseCaseImpl
import org.bazar.authorization.application.role.port.`in`.impl.UpdateRoleUseCaseImpl
import org.bazar.authorization.application.shared.AttributeExtractor
import org.bazar.authorization.application.shared.impl.DefaultUserPrincipalAttributeExtractor
import org.bazar.authorization.application.shared.impl.RolesResourceAttributeExtractor
import org.bazar.authorization.application.shared.port.out.AccessPolicyChecker
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.application.spaceuser.port.`in`.AddUserToSpaceUseCase
import org.bazar.authorization.application.spaceuser.port.`in`.AssignRoleToUserUseCase
import org.bazar.authorization.application.spaceuser.port.`in`.DeleteSpaceUseCase
import org.bazar.authorization.application.spaceuser.port.`in`.DeleteUserFromSpaceUseCase
import org.bazar.authorization.application.spaceuser.port.`in`.GetRoleNamesUseCase
import org.bazar.authorization.application.spaceuser.port.`in`.impl.AddUserToSpaceUseCaseImpl
import org.bazar.authorization.application.spaceuser.port.`in`.impl.AssignRoleToUserUseCaseImpl
import org.bazar.authorization.application.spaceuser.port.`in`.impl.DeleteSpaceUseCaseImpl
import org.bazar.authorization.application.spaceuser.port.`in`.impl.DeleteUserFromSpaceUseCaseImpl
import org.bazar.authorization.application.spaceuser.port.`in`.impl.GetRoleNamesUseCaseImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun useCaseModule() = module {
    single<AccessPolicyChecker> { CerbosAccessChecker(get()) }
    single<Authorizer> { AuthorizerAdapter(get()) }

    single<AuthorizeUseCase> { AuthorizeUseCaseImpl(get(), get(), getAll(AttributeExtractor::class)) }

    single<CreateRoleUseCase> { CreateRoleUseCaseImpl(get(), get(), get(), get(), get()) }
    single<UpdateRoleUseCase> { UpdateRoleUseCaseImpl(get(), get(), get(), get(), get()) }
    single<GetRolesInSpaceUseCase> { GetRolesInSpaceUseCaseImpl(get(), get()) }
    single<GetEnrichedRoleUseCase> { GetEnrichedRoleUseCaseImpl(get(), get(), get(), get(), get()) }

    single<AssignRoleToUserUseCase> { AssignRoleToUserUseCaseImpl(get(), get()) }
    single<AddUserToSpaceUseCase> { AddUserToSpaceUseCaseImpl(get(), get()) }
    single<DeleteUserFromSpaceUseCase> { DeleteUserFromSpaceUseCaseImpl(get(), get()) }
    single<DeleteSpaceUseCase> { DeleteSpaceUseCaseImpl(get(), get(), get(), get()) }
    single<GetRoleNamesUseCase> { GetRoleNamesUseCaseImpl(get(), get()) }

    single<GetActionsUseCase> { GetActionsUseCaseImpl(get(), get(), get()) }

    single<AttributeExtractor>(named("defaultUserPrincipalAttributeExtractor")) {
        DefaultUserPrincipalAttributeExtractor(get(), get(), get())
    }
    single<AttributeExtractor>(named("rolesResourceAttributeExtractor")) {
        RolesResourceAttributeExtractor(get())
    }
}
