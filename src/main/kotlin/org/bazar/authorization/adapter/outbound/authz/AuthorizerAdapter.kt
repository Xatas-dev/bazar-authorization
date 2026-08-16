package org.bazar.authorization.adapter.outbound.authz

import org.bazar.authorization.application.authz.port.`in`.AuthorizeUseCase
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.domain.authz.AuthorizationCheck
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException

class AuthorizerAdapter(
    private val authorizeUseCase: AuthorizeUseCase
) : Authorizer {

    override suspend fun authorizeOrThrow(check: AuthorizationCheck) {
        if (!authorizeUseCase.execute(check)) {
            throw DomainException(DomainErrors.INSUFFICIENT_PERMISSIONS)
        }
    }
}
