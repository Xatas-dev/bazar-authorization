package org.bazar.authorization.infrastructure.plugins.security

import io.grpc.Context
import io.grpc.Status
import io.ktor.server.auth.jwt.JWTPrincipal
import java.util.UUID

object GrpcSecurityContext {
    private val PRINCIPAL_KEY: Context.Key<JWTPrincipal> = Context.key("jwt-principal")
    private val TOKEN_KEY: Context.Key<String> = Context.key("jwt-raw-token")

    fun current(): JWTPrincipal? = PRINCIPAL_KEY.get()

    fun getUserId(): UUID {
        val subject = current()?.payload?.subject
            ?: throw Status.UNAUTHENTICATED.withDescription("No valid JWT principal").asException()
        return UUID.fromString(subject)
    }

    fun getRawToken(): String? = TOKEN_KEY.get()

    fun withPrincipal(principal: JWTPrincipal, rawToken: String? = null): Context =
        Context.current().withValue(PRINCIPAL_KEY, principal).withValue(TOKEN_KEY, rawToken)
}