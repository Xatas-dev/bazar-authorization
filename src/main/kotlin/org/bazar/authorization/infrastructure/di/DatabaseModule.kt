package org.bazar.authorization.infrastructure.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bazar.authorization.infrastructure.config.AppConfig
import org.bazar.authorization.infrastructure.config.DatabaseConfig
import org.bazar.authorization.infrastructure.plugins.database.LiquibaseManager
import org.koin.dsl.module
import org.koin.dsl.onClose
import javax.sql.DataSource

fun databaseModule() = module {
    single { createHikariDataSource(get<AppConfig>().db) } onClose { it?.connection?.close() }
    single { LiquibaseManager(get()) }
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
