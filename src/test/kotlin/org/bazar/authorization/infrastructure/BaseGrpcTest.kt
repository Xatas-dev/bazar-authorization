package org.bazar.authorization.infrastructure

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc.AuthorizationAdminServiceBlockingStub
import org.bazar.authorization.grpc.AuthorizationServiceGrpc
import org.bazar.authorization.grpc.AuthorizationServiceGrpc.AuthorizationServiceBlockingStub
import org.koin.test.inject

abstract class BaseGrpcTest : BaseIntegrationTest() {

    private lateinit var channel: ManagedChannel

    protected lateinit var adminStub: AuthorizationAdminServiceBlockingStub
    protected lateinit var stub: AuthorizationServiceBlockingStub
    private val appConfig: AppConfig by inject()

    fun grpcTest(
        block: suspend () -> Unit
    ) = integrationTest {

        channel = ManagedChannelBuilder
            .forAddress("localhost", appConfig.grpc.port)
            .usePlaintext()
            .directExecutor()
            .build()

        adminStub = AuthorizationAdminServiceGrpc.newBlockingStub(channel)
        stub = AuthorizationServiceGrpc.newBlockingStub(channel)

        try {
            block.invoke()
        } finally {
            channel.shutdownNow()
        }

    }
}