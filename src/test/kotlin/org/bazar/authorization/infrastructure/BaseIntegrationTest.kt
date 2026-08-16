package org.bazar.authorization.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.bazar.authorization.adapter.inbound.grpc.GrpcServerImpl
import org.bazar.authorization.adapter.outbound.role.persistence.RoleRepositoryAdapter
import org.bazar.authorization.adapter.outbound.role.persistence.RolesActionsRepositoryAdapter
import org.bazar.authorization.adapter.outbound.spaceuser.persistence.SpaceUserRepositoryAdapter
import org.bazar.authorization.infrastructure.config.TestContainers
import org.bazar.authorization.infrastructure.di.appModule
import org.bazar.authorization.infrastructure.di.cerbosModule
import org.bazar.authorization.infrastructure.di.controllerModule
import org.bazar.authorization.infrastructure.di.databaseModule
import org.bazar.authorization.infrastructure.di.grpcModule
import org.bazar.authorization.infrastructure.di.repositoryModule
import org.bazar.authorization.infrastructure.di.securityModule
import org.bazar.authorization.infrastructure.di.useCaseModule
import org.bazar.authorization.infrastructure.plugins.TEST_JWT_SECRET
import org.bazar.authorization.infrastructure.plugins.configureContentNegotiations
import org.bazar.authorization.infrastructure.plugins.configureGrpcServer
import org.bazar.authorization.infrastructure.plugins.configureRoutes
import org.bazar.authorization.infrastructure.plugins.configureSecurity
import org.bazar.authorization.infrastructure.plugins.configureStatusPages
import org.bazar.authorization.infrastructure.plugins.database.configureDatabase
import org.bazar.authorization.infrastructure.plugins.getAppConfig
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import java.util.UUID
import kotlin.getValue

abstract class BaseIntegrationTest : KoinTest {

    val initDataHelper by inject<InitDataHelper>()

    val authenticatedUserId: UUID =
        UUID.fromString("00000000-0000-0000-0000-000000000001") // from MockGrpcSecurityInterceptor

    protected fun integrationTest(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {

        client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

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
                    useCaseModule(),
                    databaseModule(),
                    controllerModule(),
                    cerbosModule(),
                    module {
                        single { SharedAppContext.cerbosClient }
                        single { SharedAppContext.hikariPool }
                        single { RoleRepositoryAdapter() }
                        single { RolesActionsRepositoryAdapter() }
                        single { SpaceUserRepositoryAdapter() }
                        single { InitDataHelper(get(), get(), get(), get()) }
                    }
                )
            }
            configureGrpcServer()
            configureContentNegotiations()
            configureSecurity()
            configureRoutes()
            configureStatusPages()
            configureDatabase()
        }

        startApplication()

        try {
            block.invoke(this)
        } finally {
            clearTables()
            get<GrpcServerImpl>().stop()
            stopKoin()
        }
    }

    protected fun authenticatedBearerToken(userId: UUID = authenticatedUserId): String {
        return JWT.create()
            .withSubject(userId.toString())
            .withIssuer("dummy")
            .sign(Algorithm.HMAC256(TEST_JWT_SECRET))
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
