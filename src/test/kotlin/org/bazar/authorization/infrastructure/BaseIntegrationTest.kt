package org.bazar.authorization.infrastructure

import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.infrastructure.config.applyTestConfiguration
import org.bazar.authorization.infrastructure.config.configureTestDatabase
import org.bazar.authorization.infrastructure.config.configureTestGrpcServer
import org.bazar.authorization.database.repository.UserSpaceRoleRepository
import org.bazar.authorization.di.appModule
import org.bazar.authorization.di.cerbosModule
import org.bazar.authorization.di.grpcModule
import org.bazar.authorization.di.repositoryModule
import org.bazar.authorization.di.securityModule
import org.bazar.authorization.di.serviceModule
import org.bazar.authorization.infrastructure.config.TestContainers
import org.bazar.authorization.plugins.configureContentNegotiations
import org.bazar.authorization.plugins.configureKoin
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.UUID


abstract class BaseIntegrationTest : KoinTest {

    val authenticatedUserId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001") // from MockGrpcSecurityInterceptor

    fun integrationTest(
        block: suspend () -> Unit
    ) = testApplication {
        environment {
            config = ApplicationConfig("application-test.yaml")
        }

        application {
            val testAppConfig = applyTestConfiguration()
            configureKoin(testAppConfig)
            configureTestDatabase()
            configureTestGrpcServer("grpc-test-server")
            configureContentNegotiations()
        }

        startApplication()

        try {
            block()
        } finally {
            clearTables()
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