package org.bazar.authorization.infrastructure.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwk.JwkProvider
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import org.bazar.authorization.infrastructure.config.AppConfig
import org.bazar.authorization.infrastructure.config.Profile
import org.koin.ktor.ext.inject

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
}
