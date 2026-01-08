package org.bazar.authorization.config.cerbos

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.CerbosClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class CerbosConfig {

    @Value($$"${cerbos.url}")
    private lateinit var cerbosUrl: String

    @Bean
    fun cerbosClient(): CerbosBlockingClient =
        CerbosClientBuilder(cerbosUrl)
            .withPlaintext().buildBlockingClient()

}