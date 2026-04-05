package org.bazar.authorization.plugins.database

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.ktor.ext.inject
import javax.sql.DataSource


fun Application.configureDatabase() {
    val dataSource by inject<DataSource>()
    val liquibaseManger by inject<LiquibaseManger>()
    Database.connect(dataSource)
    liquibaseManger.runMigrations()
    log.info("Database initialized successfully...")
}