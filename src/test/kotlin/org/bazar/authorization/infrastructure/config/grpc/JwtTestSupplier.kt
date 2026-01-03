package org.bazar.authorization.infrastructure.config.grpc

import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.UUID

class JwtTestSupplier {

    // Default values
    companion object {
        private const val DEFAULT_ISSUER = "http://localhost:9999/realms/bazar-realm"
        private const val DEFAULT_TOKEN = "test-token"
        private val DEFAULT_ROLES = listOf("USER")
        private const val DEFAULT_EXPIRY_SECONDS = 3600L
        private const val DEFAULT_ALG = "RS256"
    }

    // Mutable properties with default values
    var userId: UUID = UUID.randomUUID()
    var issuer: String = DEFAULT_ISSUER
    var token: String = DEFAULT_TOKEN
    var roles: List<String> = ArrayList(DEFAULT_ROLES)
    var expirySeconds: Long = DEFAULT_EXPIRY_SECONDS
    var algorithm: String = DEFAULT_ALG

    // Additional customizable claims
    val customClaims: MutableMap<String, Any> = mutableMapOf()

    // Additional fields for more control
    var email: String = "test@example.com"
    var username: String = "testuser"
    var audience: List<String> = listOf("bazar-client")

    /**
     * Generates a JWT with current configuration
     */
    fun getJwt(): Jwt {
        val now = Instant.now()

        return Jwt.withTokenValue(token)
            .header("alg", algorithm)
            .subject(userId.toString())
            .claim("userId", userId.toString())
            .claim("roles", roles)
            .claim("email", email)
            .claim("preferred_username", username)
            .claim("audience", audience)
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expirySeconds))
            .also { builder ->
                // Add custom claims if any
                customClaims.forEach { (key, value) ->
                    builder.claim(key, value)
                }
            }
            .build()
    }

    /**
     * Resets all properties to their default values
     */
    fun reset(): JwtTestSupplier {
        userId = UUID.randomUUID()
        issuer = DEFAULT_ISSUER
        token = DEFAULT_TOKEN
        roles = ArrayList(DEFAULT_ROLES)
        expirySeconds = DEFAULT_EXPIRY_SECONDS
        algorithm = DEFAULT_ALG
        email = "test@example.com"
        username = "testuser"
        audience = listOf("bazar-client")
        customClaims.clear()

        return this // For method chaining
    }

}