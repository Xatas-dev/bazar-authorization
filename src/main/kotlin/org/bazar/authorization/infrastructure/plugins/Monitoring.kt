package org.bazar.authorization.infrastructure.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureMonitoring() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics()
        )
    }

    routing {
        get("/actuator/health/liveness") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }

        get("/actuator/health/readiness") {
            try {
                transaction { exec("SELECT 1") {} }
                call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    mapOf("status" to "DOWN", "reason" to (e.message ?: "DB unavailable"))
                )
            }
        }

        get("/actuator/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
}
