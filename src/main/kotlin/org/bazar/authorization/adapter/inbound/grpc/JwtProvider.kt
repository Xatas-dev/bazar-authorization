package org.bazar.authorization.adapter.inbound.grpc

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.JWTPrincipal
import java.security.interfaces.RSAPublicKey

class JwtProvider(val issuer: String, val jwkProvider: JwkProvider) {
    fun verify(token: String): JWTPrincipal {
        val jwt = JWT.decode(token)
        val jwk = jwkProvider.get(jwt.keyId)
        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)

        val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .acceptLeeway(3)
            .build()

        val decoded = verifier.verify(token)
        return JWTPrincipal(decoded)
    }
}
