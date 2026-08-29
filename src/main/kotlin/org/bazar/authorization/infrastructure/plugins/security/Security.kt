package org.bazar.authorization.infrastructure.plugins.security

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.coroutines.withContext
import org.bazar.authorization.infrastructure.config.AppConfig
import org.bazar.authorization.infrastructure.config.Profile
import org.koin.ktor.ext.inject
import java.util.*

internal const val TEST_JWT_SECRET = "test-jwt-secret"

fun Application.configureSecurity() {
    val appConfig by inject<AppConfig>()
    val jwkProvider by inject<JwkProvider>()

    install(Authentication) {
        jwt("oauth") {
            if (appConfig.profile == Profile.TEST) {
                verifier(
                    JWT.require(Algorithm.HMAC256(TEST_JWT_SECRET))
                        .withIssuer(appConfig.auth.issuer)
                        .build()
                )
            } else {
                verifier(jwkProvider, appConfig.auth.issuer)
            }

            validate { credential ->
                JWTPrincipal(credential.payload)
            }
        }
    }

    intercept(ApplicationCallPipeline.Call) {
        val token = call.request.parseAuthorizationHeader()?.toString()?.removePrefix("Bearer ")
        if (token != null) {
            val userId = token.let { runCatching { JWT.decode(it).subject?.let(UUID::fromString) }.getOrNull() }
            withContext(SecurityContext(token, userId)) {
                proceed()
            }
        } else {
            proceed()
        }
    }
}
