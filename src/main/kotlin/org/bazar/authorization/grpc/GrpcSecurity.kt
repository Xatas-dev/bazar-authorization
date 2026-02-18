package org.bazar.authorization.grpc

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.grpc.*
import io.ktor.server.auth.jwt.*
import org.bazar.authorization.utils.JwtProvider
import org.bazar.authorization.utils.logger
import java.util.*

object GrpcSecurityContext {
    private val PRINCIPAL_KEY: Context.Key<JWTPrincipal> = Context.key("jwt-principal")

    fun current(): JWTPrincipal? = PRINCIPAL_KEY.get()

    fun getUserId(): UUID {
        val subject = current()?.payload?.subject
            ?: throw Status.UNAUTHENTICATED.withDescription("No valid JWT principal").asException()
        return UUID.fromString(subject)
    }

    // Helper to wrap the context
    fun withPrincipal(principal: JWTPrincipal): Context =
        Context.current().withValue(PRINCIPAL_KEY, principal)
}

class GrpcAuthInterceptor(private val jwtProvider: JwtProvider) : ServerInterceptor {
    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val methodName = call.methodDescriptor.fullMethodName
        if (methodName.startsWith("grpc.reflection") || methodName.startsWith("grpc.health")) return next.startCall(
            call,
            headers
        )

        val authHeader = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER))
        val token = authHeader?.removePrefix("Bearer ")

        return try {
            val principal = jwtProvider.verify(token ?: throw Exception("Missing Token"))
            val context = GrpcSecurityContext.withPrincipal(principal)
            Contexts.interceptCall(context, call, headers, next)
        } catch (e: Exception) {
            call.close(Status.UNAUTHENTICATED.withDescription(e.message), Metadata())
            object : ServerCall.Listener<ReqT>() {}
        }
    }
}

class MockGrpcAuthInterceptor : ServerInterceptor {

    private val logger = logger()

    init {
        logger.warn("Using MOCK authentication")
    }

    companion object {
        private val MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")
        private const val MOCK_EMAIL = "dev@localhost"
        private const val MOCK_ROLE = "ADMIN"
    }

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val methodName = call.methodDescriptor.fullMethodName
        if (methodName.startsWith("grpc.reflection") || methodName.startsWith("grpc.health")) {
            return next.startCall(call, headers)
        }

        val principal = createMockPrincipal()
        val context = GrpcSecurityContext.withPrincipal(principal)
        return Contexts.interceptCall(context, call, headers, next)
    }

    private fun createMockPrincipal(): JWTPrincipal {
        val algorithm = Algorithm.HMAC256("mock-secret")
        val token = JWT.create()
            .withSubject(MOCK_USER_ID.toString())
            .withClaim("email", MOCK_EMAIL)
            .withClaim("role", MOCK_ROLE)
            .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
            .sign(algorithm)

        val decodedJWT = JWT.decode(token)
        return JWTPrincipal(decodedJWT)
    }
}