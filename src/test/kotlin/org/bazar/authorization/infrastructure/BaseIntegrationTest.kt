package org.bazar.authorization.infrastructure

import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.*
import org.bazar.authorization.di.appModule
import org.bazar.authorization.di.grpcModule
import org.bazar.authorization.di.repositoryModule
import org.bazar.authorization.di.securityModule
import org.bazar.authorization.di.serviceModule
import org.bazar.authorization.infrastructure.config.TestContainers
import org.bazar.authorization.plugins.configureContentNegotiations
import org.bazar.authorization.plugins.configureGrpcServer
import org.bazar.authorization.plugins.getAppConfig
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.test.KoinTest
import java.util.*


abstract class BaseIntegrationTest : KoinTest {

    val authenticatedUserId: UUID =
        UUID.fromString("00000000-0000-0000-0000-000000000001") // from MockGrpcSecurityInterceptor

    companion object {
        init {
            TestDatabase.initOnce()
        }
    }

    protected fun integrationTest(
        block: suspend () -> Unit
    ) = testApplication {

        environment {
            config = ApplicationConfig("application-test.yaml")
        }

        application {
            val testAppConfig = getAppConfig()
            install(Koin) {
                slf4jLogger()
                modules(
                    appModule(testAppConfig),
                    securityModule(),
                    grpcModule(),
                    repositoryModule(),
                    serviceModule(),
                    module { single { TestContainers.getCerbosClient() } }
                )
            }
            configureGrpcServer()
            configureContentNegotiations()
        }

        startApplication()

        try {
            block()
        } finally {
            clearTables()
            stopKoin()
        }
    }

    private fun clearTables() {
        TestContainers.postgres.createConnection("").use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("TRUNCATE TABLE user_space_role CASCADE")
            }
        }
    }
}