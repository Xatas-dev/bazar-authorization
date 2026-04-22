package org.bazar.authorization.infrastructure

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.assertj.core.api.Assertions.assertThat
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.database.repository.ActionRepository
import org.bazar.authorization.database.repository.RoleRepository
import org.bazar.authorization.database.repository.RolesActionsRepository
import org.bazar.authorization.database.repository.SpaceUserRepository
import org.bazar.authorization.database.tables.SpaceUsers
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc.AuthorizationAdminServiceBlockingStub
import org.bazar.authorization.grpc.AuthorizationServiceGrpc
import org.bazar.authorization.grpc.AuthorizationServiceGrpc.AuthorizationServiceBlockingStub
import org.bazar.authorization.utils.buildRole
import org.bazar.authorization.utils.buildRolesActions
import org.bazar.authorization.utils.buildSpaceUser
import org.bazar.authorization.utils.extensions.toSpaceUserEntity
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.assertThrows
import org.koin.test.inject
import java.util.*
import kotlin.math.abs

abstract class BaseGrpcTest : BaseIntegrationTest() {

    private lateinit var channel: ManagedChannel

    protected lateinit var adminStub: AuthorizationAdminServiceBlockingStub
    protected lateinit var stub: AuthorizationServiceBlockingStub
    private val appConfig: AppConfig by inject()
    protected val spaceUserRepository by inject<SpaceUserRepository>()
    protected val rolesActionsRepository by inject<RolesActionsRepository>()
    protected val roleRepository by inject<RoleRepository>()
    protected val actionsRepository by inject<ActionRepository>()

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

    protected fun createSpaceUser(spaceId: Long, userId: UUID, roleId: Long, creator: Boolean) = transaction {
        spaceUserRepository.save(buildSpaceUser(spaceId, userId, roleId, creator))
    }

    protected fun createRolesActions(roleId: Long, actionId: Int) = transaction {
        rolesActionsRepository.save(buildRolesActions(roleId, actionId))
    }


    protected fun getAllActions() = transaction {
        actionsRepository.getAllActions()
    }

    protected fun getActionId(code: String, resource: String) = transaction {
        actionsRepository.findByCodeAndResource(code, resource)!!.id
    }

    protected fun createRole(): Long = transaction {
        roleRepository.save(buildRole(RoleScope.USER)).id!!
    }

    protected fun getAllSpaceUsers(spaceId: Long): List<SpaceUserEntity> {
        return transaction {
            SpaceUsers.selectAll()
                .where { SpaceUsers.spaceId eq spaceId }
                .map { it.toSpaceUserEntity() }
        }
    }

    protected fun assertGrpcStatus(expected: Status, call: () -> Unit) {
        val error = assertThrows<StatusRuntimeException>(call)
        assertThat(error.status.code).isEqualTo(expected.code)
    }

    protected fun randomSpaceId(): Long = abs(UUID.randomUUID().mostSignificantBits)
}