package org.bazar.authorization.infrastructure

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.bazar.authorization.di.*
import org.bazar.authorization.grpc.GrpcServerImpl
import org.bazar.authorization.infrastructure.config.TestContainers
import org.bazar.authorization.plugins.configureContentNegotiations
import org.bazar.authorization.plugins.configureGrpcServer
import org.bazar.authorization.plugins.database.configureDatabase
import org.bazar.authorization.plugins.getAppConfig
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.test.KoinTest
import org.koin.test.get
import java.util.*


abstract class BaseIntegrationTest : KoinTest {

    val authenticatedUserId: UUID =
        UUID.fromString("00000000-0000-0000-0000-000000000001") // from MockGrpcSecurityInterceptor

    protected fun integrationTest(
        block: suspend () -> Unit
    ) = testApplication {

        environment {
            config = ApplicationConfig("application-test.yaml")
        }

        application {
            val testAppConfig = getAppConfig()
                .apply {
                    db.user = TestContainers.postgres.username
                    db.password = TestContainers.postgres.password
                    db.jdbcUrl = TestContainers.postgres.jdbcUrl

                    cerbos.url = TestContainers.cerbos.target
                }
            install(Koin) {
                slf4jLogger()
                allowOverride(true)
                modules(
                    appModule(testAppConfig),
                    securityModule(),
                    grpcModule(),
                    repositoryModule(),
                    serviceModule(),
                    databaseModule(),
                    module {
                        single { SharedAppContext.cerbosClient }
                        single { SharedAppContext.hikariPool }
                    }

                )
            }
            configureGrpcServer()
            configureContentNegotiations()
            configureDatabase()
        }

        startApplication()

        try {
            block()
        } finally {
            clearTables()
            get<GrpcServerImpl>().stop()
            stopKoin()
        }
    }

    private fun clearTables() {
        val sql = this::class.java.getResourceAsStream("/sql/clear-tables.sql")
            ?.bufferedReader()
            ?.readText()
            ?: throw IllegalStateException("clear-tables.sql not found")

        TestContainers.postgres.createConnection("").use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(sql)
            }
        }
    }
}