package org.bazar.authorization.plugins.database

import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class LiquibaseManager(
    private val dataSource: DataSource
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun runMigrations() {
        logger.info("Running Liquibase migrations...")

        dataSource.connection.use { connection ->
            Liquibase(
                "db/changelog/changelog-master.yaml",
                ClassLoaderResourceAccessor(),
                JdbcConnection(connection)
            ).use { liquibase ->
                liquibase.update("")
            }
        }

        logger.info("Liquibase migrations completed.")
    }

}