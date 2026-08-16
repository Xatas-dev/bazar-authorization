package org.bazar.authorization.application.authz.port.`in`

import org.bazar.authorization.domain.authz.AuthorizationCheck

interface AuthorizeUseCase {
    suspend fun execute(check: AuthorizationCheck): Boolean
}
