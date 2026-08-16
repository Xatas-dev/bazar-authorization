package org.bazar.authorization.infrastructure

import com.zaxxer.hikari.HikariDataSource
import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.CerbosClientBuilder
import org.bazar.authorization.infrastructure.config.TestContainers
import org.bazar.authorization.infrastructure.plugins.database.LiquibaseManager
import javax.sql.DataSource

object SharedAppContext {

    val cerbosClient: CerbosBlockingClient = CerbosClientBuilder(TestContainers.cerbos.target)
        .withPlaintext()
        .buildBlockingClient()

    val hikariPool: DataSource = HikariDataSource().apply {
        jdbcUrl = TestContainers.postgres.jdbcUrl
        username = TestContainers.postgres.username
        password = TestContainers.postgres.password
    }

    fun runMigrations() {
        LiquibaseManager(hikariPool).runMigrations()
    }

    init {
        runMigrations()
    }

}