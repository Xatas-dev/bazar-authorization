package org.bazar.authorization.infrastructure.config.grpc

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptor
import io.grpc.health.v1.HealthCheckResponse
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.protobuf.services.HealthStatusManager
import io.grpc.protobuf.services.ProtoReflectionServiceV1
import org.bazar.authorization.utils.logger
import org.slf4j.Logger
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach

class TestGrpcServer(
    private val interceptors: List<ServerInterceptor>,
    private val services: List<BindableService>,
    private val serverName: String
) {

    private val logger: Logger = logger()
    private lateinit var server: Server

    fun start() {
        server = InProcessServerBuilder.forName(serverName)
            .apply { interceptors.forEach { intercept(it) } }
            .apply { services.forEach { addService(it) } }
            .build()
            .start()

        logger.info("TEST gRPC Server started")
    }

    fun stop() {
        if (::server.isInitialized) {
            server.shutdown()
            try {
                if (!server.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.warn("gRPC Server didn't terminate gracefully, forcing shutdown")
                    server.shutdownNow()
                    server.awaitTermination(5, TimeUnit.SECONDS)
                }
            } catch (e: InterruptedException) {
                server.shutdownNow()
                Thread.currentThread().interrupt()
            }
            logger.info("gRPC Server stopped.")
        }
    }
}