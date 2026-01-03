package org.bazar.authorization.infrastructure.config.cerbos

import org.bazar.authorization.infrastructure.BaseIntegrationTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class CerbosTestConfig {

    @Bean
    @Primary
    fun cerbosClient() = BaseIntegrationTest.cerbosClient()


}