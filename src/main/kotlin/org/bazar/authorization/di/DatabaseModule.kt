package org.bazar.authorization.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.config.DatabaseConfig
import org.bazar.authorization.plugins.database.LiquibaseManger
import org.koin.dsl.module
import javax.sql.DataSource

fun databaseModule() = module {
    single { createHikariDataSource(get<AppConfig>().db) }
    single { LiquibaseManger(get())}
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