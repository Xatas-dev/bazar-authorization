package org.bazar.authorization.infrastructure.plugins

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.bazar.authorization.infrastructure.config.AppConfig
import org.bazar.authorization.infrastructure.config.AuthConfig
import org.bazar.authorization.infrastructure.config.CerbosConfig
import org.bazar.authorization.infrastructure.config.DatabaseConfig
import org.bazar.authorization.infrastructure.config.GrpcConfig
import org.bazar.authorization.infrastructure.config.LoggingConfig
import org.bazar.authorization.infrastructure.config.Profile
import org.slf4j.LoggerFactory

fun Application.getAppConfig(): AppConfig {
    val appConfig = environment.config.toAppConfig()
    applyLoggingLevels(appConfig)
    return appConfig
}

private fun ApplicationConfig.toAppConfig(): AppConfig {
    val loggingMap = mutableMapOf<String, String>()
    config("logging.level").keys().forEach { key ->
        loggingMap[key] = property("logging.level.$key").getString()
    }
    return AppConfig(
        db = DatabaseConfig(
            property("db.jdbcUrl").getString(),
            property("db.user").getString(),
            property("db.password").getString(),
            property("db.logSqlQueries").getAs<Boolean>(),
            property("db.runMigrations").getAs<Boolean>()
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

private fun applyLoggingLevels(appConfig: AppConfig) {
    val levels = appConfig.logging.level
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

    levels.forEach { (loggerName, levelStr) ->
        val logger = if (loggerName == "root") {
            loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        } else {
            loggerContext.getLogger(loggerName)
        }

        logger.level = Level.toLevel(levelStr, Level.INFO)
    }
}
