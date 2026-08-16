package org.bazar.authorization.infrastructure

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.assertj.core.api.Assertions.assertThat
import org.bazar.authorization.adapter.outbound.role.persistence.RoleRepositoryAdapter
import org.bazar.authorization.adapter.outbound.role.persistence.RolesActionsRepositoryAdapter
import org.bazar.authorization.adapter.outbound.spaceuser.persistence.SpaceUserRepositoryAdapter
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc.AuthorizationAdminServiceBlockingStub
import org.bazar.authorization.grpc.AuthorizationServiceGrpc
import org.bazar.authorization.grpc.AuthorizationServiceGrpc.AuthorizationServiceBlockingStub
import org.bazar.authorization.infrastructure.config.AppConfig
import org.junit.jupiter.api.assertThrows
import org.koin.test.inject
import java.util.UUID
import kotlin.math.abs

abstract class BaseGrpcTest : BaseIntegrationTest() {

    private lateinit var channel: ManagedChannel

    protected lateinit var adminStub: AuthorizationAdminServiceBlockingStub
    protected lateinit var stub: AuthorizationServiceBlockingStub
    private val appConfig: AppConfig by inject()
    protected val spaceUserRepository by inject<SpaceUserRepositoryAdapter>()
    protected val rolesActionsRepository by inject<RolesActionsRepositoryAdapter>()
    protected val roleRepository by inject<RoleRepositoryAdapter>()

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

    protected fun assertGrpcStatus(expected: Status, call: () -> Unit) {
        val error = assertThrows<StatusRuntimeException>(call)
        assertThat(error.status.code).isEqualTo(expected.code)
    }

    protected fun randomSpaceId(): Long = abs(UUID.randomUUID().mostSignificantBits)
}
