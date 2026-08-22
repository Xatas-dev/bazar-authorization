package org.bazar.authorization.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import io.mockk.mockk
import org.bazar.authorization.adapter.inbound.grpc.GrpcServerImpl
import org.bazar.authorization.adapter.outbound.http.BazarSpaceHttpClient
import org.bazar.authorization.adapter.outbound.role.persistence.RoleRepositoryAdapter
import org.bazar.authorization.adapter.outbound.role.persistence.RolesActionsRepositoryAdapter
import org.bazar.authorization.adapter.outbound.spaceuser.persistence.SpaceUserRepositoryAdapter
import org.bazar.authorization.infrastructure.config.TestContainers
import org.bazar.authorization.infrastructure.di.*
import org.bazar.authorization.infrastructure.plugins.*
import org.bazar.authorization.infrastructure.plugins.database.configureDatabase
import org.bazar.authorization.infrastructure.plugins.security.TEST_JWT_SECRET
import org.bazar.authorization.infrastructure.plugins.security.configureSecurity
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import java.util.*

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
                    outboundModule(),
                    cerbosModule(),
                    testInfraModule(),
                    mockkModule()
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

    private fun mockkModule() = module {
        single<BazarSpaceHttpClient> { mockk<BazarSpaceHttpClient>() }
    }

    private fun testInfraModule() = module {
        single { SharedAppContext.cerbosClient }
        single { SharedAppContext.hikariPool }
        single { RoleRepositoryAdapter() }
        single { RolesActionsRepositoryAdapter() }
        single { SpaceUserRepositoryAdapter() }
        single { InitDataHelper(get(), get(), get(), get()) }
    }
}
