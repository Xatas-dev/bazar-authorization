package org.bazar.authorization.infrastructure.config

data class AppConfig(
    val db: DatabaseConfig,
    val auth: AuthConfig,
    val grpc: GrpcConfig,
    val cerbos: CerbosConfig,
    val profile: Profile,
    val logging: LoggingConfig
)

data class LoggingConfig(var level: Map<String, String> = emptyMap())
data class DatabaseConfig(
    var jdbcUrl: String,
    var user: String,
    var password: String,
    var logSqlQueries: Boolean,
    var runMigrations: Boolean
)

data class AuthConfig(var issuer: String, var jwkUrl: String)
data class GrpcConfig(var port: Int)
data class CerbosConfig(var url: String)
