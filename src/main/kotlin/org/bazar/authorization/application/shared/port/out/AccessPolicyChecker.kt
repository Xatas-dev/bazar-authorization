package org.bazar.authorization.application.shared.port.out

import org.bazar.authorization.domain.authz.AuthorizationCheck

interface AccessPolicyChecker {
    suspend fun checkAccess(check: AuthorizationCheck): Boolean
}
