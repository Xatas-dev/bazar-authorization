package org.bazar.authorization.adapter.outbound.authentication.grpc

import org.bazar.authorization.application.shared.port.out.AuthContextPort
import org.bazar.authorization.infrastructure.plugins.security.GrpcSecurityContext
import java.util.UUID

class GrpcAuthContextProvider : AuthContextPort {

    override suspend fun getToken(): String? = GrpcSecurityContext.getRawToken()

    override suspend fun getUserId(): UUID? =
        GrpcSecurityContext.current()?.payload?.subject?.let { UUID.fromString(it) }
}
