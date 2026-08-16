package org.bazar.authorization.adapter.inbound.grpc

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptor
import io.grpc.health.v1.HealthCheckResponse
import io.grpc.protobuf.services.HealthStatusManager
import io.grpc.protobuf.services.ProtoReflectionServiceV1
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class GrpcServerImpl(
    private val port: Int,
    private val interceptors: List<ServerInterceptor>,
    private val services: List<BindableService>
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val server: Server
    private val healthStatusManager = HealthStatusManager()

    init {
        server = initServer()
    }

    fun start() {
        server.start()

        setServingStatus(HealthCheckResponse.ServingStatus.SERVING)

        logger.info("gRPC Server started on port $port")
    }

    fun stop() {
        setServingStatus(HealthCheckResponse.ServingStatus.NOT_SERVING)

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

    private fun setServingStatus(status: HealthCheckResponse.ServingStatus) {
        healthStatusManager.setStatus("", status)
        services.forEach { service ->
            val serviceName = service.bindService().serviceDescriptor.name
            healthStatusManager.setStatus(serviceName, status)
        }
    }

    private fun initServer(): Server {
        return ServerBuilder.forPort(port)
            .apply { interceptors.forEach { intercept(it) } }
            .apply { services.forEach { addService(it) } }
            .addService(ProtoReflectionServiceV1.newInstance())
            .addService(healthStatusManager.healthService)
            .build()
    }
}
