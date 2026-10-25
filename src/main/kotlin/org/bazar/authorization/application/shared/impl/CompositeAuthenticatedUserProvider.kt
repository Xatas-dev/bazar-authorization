package org.bazar.authorization.application.shared.impl

import org.bazar.authorization.application.shared.AuthenticatedUserProvider
import org.bazar.authorization.application.shared.port.out.AuthContextPort
import org.bazar.authorization.domain.exception.UnauthenticatedException
import java.util.*

class CompositeAuthenticatedUserProvider(
    private val providers: List<AuthContextPort>
) : AuthenticatedUserProvider {

    override suspend fun getToken(): String =
        providers.firstNotNullOfOrNull { it.getToken() } ?: throw UnauthenticatedException()

    override suspend fun getUserId(): UUID =
        providers.firstNotNullOfOrNull { it.getUserId() } ?: throw UnauthenticatedException()
}
