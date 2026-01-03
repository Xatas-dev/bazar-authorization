package org.bazar.authorization.config.grpc

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.bazar.authorization.config.grpc.interceptors.KotlinServerInterceptor
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.JwkSetUriJwtDecoderBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.grpc.server.GlobalServerInterceptor
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor
import org.springframework.grpc.server.security.GrpcSecurity
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder

@Configuration
@EnableConfigurationProperties(OAuth2ResourceServerProperties::class)
@Import(AuthenticationConfiguration::class)
class GrpcServerSecurityConfig(
    @Value($$"${spring.grpc.server.security.enabled:true}")
    private val securityEnabled: Boolean,
    private val properties: OAuth2ResourceServerProperties
) {
    private val logger: KLogger = KotlinLogging.logger { }

    init {
        logger.info { "gRPC server security is ${if (securityEnabled) "ON" else "OFF"}" }
    }

    @Bean
    @GlobalServerInterceptor
    @Throws(Exception::class)
    @ConditionalOnProperty("spring.grpc.server.security.enabled", havingValue = "true", matchIfMissing = true)
    fun jwtSecurityFilterChain(
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
        customizers: ObjectProvider<JwkSetUriJwtDecoderBuilderCustomizer>
    ): SupplierJwtDecoder {
        return SupplierJwtDecoder {
            val issuerUri: String? = this.properties.jwt.issuerUri
            val builder = NimbusJwtDecoder.withIssuerLocation(issuerUri)
            customizers.orderedStream()
                .forEach { customizer: JwkSetUriJwtDecoderBuilderCustomizer? -> customizer!!.customize(builder) }
            val jwtDecoder = builder.build()
            jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri))
            jwtDecoder
        }
    }

    @Bean
    @GlobalServerInterceptor
    @ConditionalOnProperty("spring.grpc.server.security.enabled", havingValue = "true", matchIfMissing = true)
    fun kotlinSecurityContextInterceptor(): ServerInterceptor {
        return KotlinServerInterceptor()
    }

}
