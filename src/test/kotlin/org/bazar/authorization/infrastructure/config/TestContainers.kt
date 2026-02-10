package org.bazar.authorization.infrastructure.config

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.CerbosClientBuilder
import dev.cerbos.sdk.CerbosContainer
import org.bazar.authorization.utils.logger
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.lifecycle.Startables
import org.testcontainers.utility.DockerImageName

object TestContainers {

    private val logger = logger()

    val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:16.0"))
        .withDatabaseName("testDb")
        .withUsername("test")
        .withPassword("test")

    val cerbos: CerbosContainer = CerbosContainer()
        .withClasspathResourceMapping("cerbos/policies", "/policies", BindMode.READ_ONLY)
        .withLogConsumer(Slf4jLogConsumer(logger))

    init {
        Startables.deepStart(postgres, cerbos).join()
    }
}