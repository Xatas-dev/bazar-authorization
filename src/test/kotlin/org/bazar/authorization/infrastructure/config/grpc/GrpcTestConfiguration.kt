package org.bazar.authorization.infrastructure.config.grpc

import io.grpc.ClientInterceptor
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.grpc.client.GlobalClientInterceptor
import org.springframework.grpc.client.interceptor.security.BearerTokenAuthenticationInterceptor
import org.springframework.security.oauth2.jwt.JwtDecoder

@TestConfiguration
class GrpcTestConfiguration {


    @Bean
    @GlobalClientInterceptor
    fun bearerInterceptor(): ClientInterceptor {
        return BearerTokenAuthenticationInterceptor { "test-token" }
    }

    @Bean
    fun jwtTestSupplier(): JwtTestSupplier = JwtTestSupplier()

    @Bean
    @Primary // Overrides the real JwtDecoder in the test context
    fun testJwtDecoder(jwtTestSupplier: JwtTestSupplier): JwtDecoder {
        return JwtDecoder { token ->
            val jwt = jwtTestSupplier.getJwt()
            jwtTestSupplier.reset()
            return@JwtDecoder jwt
        }
    }

}