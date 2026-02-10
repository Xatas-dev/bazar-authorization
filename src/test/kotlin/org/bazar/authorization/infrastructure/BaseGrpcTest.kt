package org.bazar.authorization.infrastructure

import io.grpc.ManagedChannel
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.testing.GrpcCleanupRule
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc.AuthorizationAdminServiceBlockingStub
import org.bazar.authorization.grpc.AuthorizationServiceGrpc
import org.bazar.authorization.grpc.AuthorizationServiceGrpc.AuthorizationServiceBlockingStub
import org.bazar.authorization.grpc.SpaceAdminAuthorizationService
import org.bazar.authorization.grpc.SpaceAuthorizationService
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.test.inject

abstract class BaseGrpcTest : BaseIntegrationTest() {

    private lateinit var channel: ManagedChannel

    protected lateinit var adminStub: AuthorizationAdminServiceBlockingStub
    protected lateinit var stub: AuthorizationServiceBlockingStub

    @BeforeEach
    fun setupGrpc() {

        channel = InProcessChannelBuilder
            .forName("grpc-test-server")
            .directExecutor()
            .build()

        adminStub = AuthorizationAdminServiceGrpc.newBlockingStub(channel)
        stub = AuthorizationServiceGrpc.newBlockingStub(channel)
    }

    @AfterEach
    fun tearDownGrpc() {
        channel.shutdownNow()
    }
}