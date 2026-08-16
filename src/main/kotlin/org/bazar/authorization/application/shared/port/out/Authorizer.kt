package org.bazar.authorization.application.shared.port.out

import org.bazar.authorization.domain.authz.AuthorizationCheck

interface Authorizer {
    suspend fun authorizeOrThrow(check: AuthorizationCheck)
}
