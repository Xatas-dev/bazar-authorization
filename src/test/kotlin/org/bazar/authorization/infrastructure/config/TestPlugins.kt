package org.bazar.authorization.infrastructure.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.grpc.BindableService
import io.grpc.ServerInterceptor
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.log
import io.ktor.server.config.ApplicationConfig
import io.ktor.util.logging.Logger
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.config.AuthConfig
import org.bazar.authorization.config.CerbosConfig
import org.bazar.authorization.config.DatabaseConfig
import org.bazar.authorization.config.GrpcConfig
import org.bazar.authorization.config.LoggingConfig
import org.bazar.authorization.config.applyLoggingLevels
import org.bazar.authorization.grpc.SpaceAdminAuthorizationService
import org.bazar.authorization.grpc.SpaceAuthorizationService
import org.bazar.authorization.infrastructure.config.grpc.TestGrpcServer
import org.bazar.authorization.utils.Profile
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import javax.sql.DataSource
import kotlin.getValue

fun Application.configureTestDatabase() {
    val appConfig by inject<AppConfig>()
    initDb(appConfig.db, log)
}

private fun initDb(config: DatabaseConfig, logger: Logger) {
    val pool = createHikariDataSource(config)
    runMigrations(pool, logger)
    Database.connect(pool)
    logger.info("Test database initialization completed successfully.")
}

private fun runMigrations(dataSource: DataSource, logger: Logger) {
    logger.info("Running Liquibase migrations...")

    dataSource.connection.use { connection ->
        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(connection))

        Liquibase(
            "db/changelog/changelog-master.yaml",
            ClassLoaderResourceAccessor(),
            database
        ).use { liquibase ->
            liquibase.update("")
        }
    }

    logger.info("Liquibase migrations completed.")
}

private fun createHikariDataSource(config: DatabaseConfig): DataSource {
    val hikariConfig = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = config.jdbcUrl
        username = config.user
        password = config.password
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_READ_COMMITTED"

        validate()
    }
    return HikariDataSource(hikariConfig)
}

fun Application.applyTestConfiguration(): AppConfig {
    val appConfig = environment.config.toTestAppConfig()
    applyLoggingLevels(appConfig)
    return appConfig
}


fun ApplicationConfig.toTestAppConfig(): AppConfig {
    val loggingMap = mutableMapOf<String, String>()
    config("logging.level").keys().forEach { key ->
        loggingMap[key] = property("logging.level.$key").getString()
    }
    return AppConfig(
        db = DatabaseConfig(
            TestContainers.postgres.jdbcUrl,
            TestContainers.postgres.username,
            TestContainers.postgres.password
        ),
        auth = AuthConfig(issuer = "test", jwkUrl = "test"),
        grpc = GrpcConfig(property("grpc.port").getString().toInt()),
        cerbos = CerbosConfig(
            url = TestContainers.cerbos.target
        ),
        profile = Profile.valueOf(property("profile").getString()),
        logging = LoggingConfig(loggingMap)
    )
}


fun Application.configureTestGrpcServer(serverName: String) {
    val authService by inject<SpaceAuthorizationService>()
    val adminAuthService by inject<SpaceAdminAuthorizationService>()
    val interceptors = getKoin().getAll<ServerInterceptor>()
    val server = TestGrpcServer(
        interceptors,
        services = listOf(authService, adminAuthService),
        serverName = serverName
    )

    monitor.subscribe(ApplicationStarted) {
        server.start()
    }
    monitor.subscribe(ApplicationStopped) {
        server.stop()
    }
}