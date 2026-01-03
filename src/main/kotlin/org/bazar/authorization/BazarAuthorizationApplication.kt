package org.bazar.authorization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity

@SpringBootApplication
class BazarAuthorizationApplication

fun main(args: Array<String>) {
    runApplication<BazarAuthorizationApplication>(*args)
}
