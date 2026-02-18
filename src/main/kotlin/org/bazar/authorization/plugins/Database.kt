package org.bazar.authorization.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.util.logging.Logger
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.config.DatabaseConfig
import org.bazar.authorization.utils.Profile
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.ktor.ext.inject
import javax.sql.DataSource
import kotlin.use


fun Application.configureDatabase() {
    val appConfig by inject<AppConfig>()
    initDb(appConfig, log)
}

private fun initDb(config: AppConfig, logger: Logger) {
    val pool = createHikariDataSource(config.db)
    if (config.profile != Profile.PROD){
        runMigrations(pool, logger)
    }
    Database.connect(pool)
    logger.info("Database initialization completed successfully.")
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