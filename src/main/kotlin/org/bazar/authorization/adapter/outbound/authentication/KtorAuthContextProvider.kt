package org.bazar.authorization.adapter.outbound.authentication

import kotlinx.coroutines.currentCoroutineContext
import org.bazar.authorization.application.shared.port.out.AuthContextPort
import org.bazar.authorization.infrastructure.plugins.security.SecurityContext
import java.util.UUID

class KtorAuthContextProvider : AuthContextPort {

    override suspend fun getToken(): String? =
        currentCoroutineContext()[SecurityContext]?.token

    override suspend fun getUserId(): UUID? =
        currentCoroutineContext()[SecurityContext]?.userId
}
