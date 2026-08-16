package org.bazar.authorization.infrastructure.plugins.database

import io.ktor.server.application.*
import org.bazar.authorization.infrastructure.config.AppConfig
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.ktor.ext.inject
import javax.sql.DataSource

fun Application.configureDatabase() {
    val dataSource by inject<DataSource>()
    val liquibaseManager by inject<LiquibaseManager>()
    val appConfig by inject<AppConfig>()
    Database.connect(
        dataSource,
        databaseConfig = DatabaseConfig {
            if (appConfig.db.logSqlQueries)
                sqlLogger = StdOutSqlLogger
        }
    )

    if (appConfig.db.runMigrations)
        liquibaseManager.runMigrations()

    log.info("Database initialized successfully...")
}
