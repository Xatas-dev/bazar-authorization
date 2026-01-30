package org.bazar.authorization.config.grpc

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerInterceptor
import org.bazar.authorization.config.grpc.interceptors.KotlinServerInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.grpc.server.GlobalServerInterceptor
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor
import org.springframework.grpc.server.security.GrpcSecurity
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder

@Configuration
@EnableConfigurationProperties(OAuth2ResourceServerProperties::class)
@Import(AuthenticationConfiguration::class)
class GrpcServerSecurityConfig(
    @Value($$"${spring.grpc.server.security.enabled:true}")
    private val securityEnabled: Boolean
) {
    private val logger: KLogger = KotlinLogging.logger { }

    init {
        logger.info { "gRPC server security is ${if (securityEnabled) "ON" else "OFF"}" }
    }

    @Bean
    @GlobalServerInterceptor
    @Throws(Exception::class)
    @ConditionalOnProperty("spring.grpc.server.security.enabled", havingValue = "true", matchIfMissing = true)
    fun grpcFilterSecurityChain(
        grpc: GrpcSecurity
    ): AuthenticationProcessInterceptor {
        return grpc
            .authorizeRequests { requests ->

                requests.methods("bazar.*/*")
                    .authenticated()
                    .methods("grpc.*/*")
                    .permitAll()
                requests.allRequests()
                    .denyAll()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt(Customizer.withDefaults())
            }
            .build()
    }

    @Bean
    @ConditionalOnProperty("spring.grpc.server.security.enabled", havingValue = "true", matchIfMissing = true)
    fun blockingJwtDecoderByIssuerUri(
        properties: OAuth2ResourceServerProperties
    ): SupplierJwtDecoder {
        return SupplierJwtDecoder {
            JwtDecoders.fromIssuerLocation(properties.jwt.issuerUri)
        }
    }

    @Bean
    @GlobalServerInterceptor
    @ConditionalOnProperty("spring.grpc.server.security.enabled", havingValue = "true", matchIfMissing = true)
    fun kotlinSecurityContextInterceptor(): ServerInterceptor {
        return KotlinServerInterceptor()
    }

}
