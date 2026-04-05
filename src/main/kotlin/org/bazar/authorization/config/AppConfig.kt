package org.bazar.authorization.config

import io.ktor.server.config.*
import org.bazar.authorization.utils.Profile

data class AppConfig(
    val db: DatabaseConfig,
    val auth: AuthConfig,
    val grpc: GrpcConfig,
    val cerbos: CerbosConfig,
    val profile: Profile,
    val logging: LoggingConfig
)

data class LoggingConfig(val level: Map<String, String> = emptyMap())
data class DatabaseConfig(val jdbcUrl: String, val user: String, val password: String)
data class AuthConfig(val issuer: String, val jwkUrl: String)
data class GrpcConfig(val port: Int)
data class CerbosConfig(val url: String)