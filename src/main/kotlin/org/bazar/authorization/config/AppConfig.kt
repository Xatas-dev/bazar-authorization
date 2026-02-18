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

fun ApplicationConfig.toAppConfig(): AppConfig {
    val loggingMap = mutableMapOf<String, String>()
    config("logging.level").keys().forEach { key ->
        loggingMap[key] = property("logging.level.$key").getString()
    }
    return AppConfig(
        db = DatabaseConfig(
            property("db.jdbcUrl").getString(),
            property("db.user").getString(),
            property("db.password").getString()
        ),
        auth = AuthConfig(
            property("auth.issuer").getString(),
            property("auth.jwkUrl").getString(),
        ),
        grpc = GrpcConfig(property("grpc.port").getString().toInt()),
        cerbos = CerbosConfig(property("cerbos.url").getString()),
        profile = Profile.valueOf(property("profile").getString()),
        logging = LoggingConfig(loggingMap)
    )
}