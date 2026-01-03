package org.bazar.authorization.config.cerbos

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.CerbosClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class CerbosConfig {

    @Bean
    fun cerbosClient(): CerbosBlockingClient =
        CerbosClientBuilder("localhost:3592")
            .withPlaintext().buildBlockingClient()

}