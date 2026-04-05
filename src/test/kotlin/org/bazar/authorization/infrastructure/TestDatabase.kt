package org.bazar.authorization.infrastructure

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.bazar.authorization.config.DatabaseConfig
import org.bazar.authorization.infrastructure.config.TestContainers
import org.jetbrains.exposed.v1.jdbc.Database
import javax.sql.DataSource

object TestDatabase {

    private var initialized = false

    fun initOnce() {
        if (initialized) return

        val pool = createHikariDataSource(TestContainers.postgres.let {
            DatabaseConfig(it.jdbcUrl, it.username, it.password)
        })
        runMigrations(pool)
        Database.connect(pool)

        initialized = true
    }

    private fun runMigrations(dataSource: DataSource) {
        dataSource.connection.use { connection ->
            val database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(connection))
            Liquibase(
                "db/changelog/changelog-master.yaml",
                ClassLoaderResourceAccessor(),
                database
            ).use { it.update("") }
        }
    }

    private fun createHikariDataSource(config: DatabaseConfig): DataSource {
        return HikariDataSource(HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = config.jdbcUrl
            username = config.user
            password = config.password
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            validate()
        })
    }
}