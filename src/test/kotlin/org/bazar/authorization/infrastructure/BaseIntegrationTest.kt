package org.bazar.authorization.infrastructure

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.CerbosClientBuilder
import dev.cerbos.sdk.CerbosContainer
import org.bazar.authorization.infrastructure.config.cerbos.CerbosTestConfig
import org.bazar.authorization.infrastructure.config.grpc.JwtTestSupplier
import org.bazar.authorization.persistence.repository.UserSpaceRoleRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.lifecycle.Startables
import org.testcontainers.utility.DockerImageName
import java.util.stream.Stream


@SpringBootTest
@ActiveProfiles("test")
@Sql("classpath:db/scripts/clearTables.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Import(CerbosTestConfig::class)
abstract class BaseIntegrationTest {

    @Autowired
    lateinit var userSpaceRoleRepository: UserSpaceRoleRepository

    @Autowired
    lateinit var jwtTestSupplier: JwtTestSupplier

    companion object {

        private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:16.0"))
            .apply {
                this.withDatabaseName("testDb").withUsername("test").withPassword("test")
            }

        private val cerbosContainer: CerbosContainer = CerbosContainer()
            .withClasspathResourceMapping("cerbos/policies", "/policies", BindMode.READ_ONLY)
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(BaseIntegrationTest::class.java)))

        fun cerbosClient(): CerbosBlockingClient {
            val target = cerbosContainer.target
            return CerbosClientBuilder(target).withPlaintext().buildBlockingClient()
        }

        @JvmStatic
        @DynamicPropertySource
        fun datasourceConfig(registry: DynamicPropertyRegistry) {
            Startables.deepStart(Stream.of(postgres, cerbosContainer)).join()

            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }

    }
}