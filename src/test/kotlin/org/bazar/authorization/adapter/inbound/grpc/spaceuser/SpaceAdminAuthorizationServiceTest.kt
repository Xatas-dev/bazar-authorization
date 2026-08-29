package org.bazar.authorization.adapter.inbound.grpc.spaceuser

import io.grpc.Status
import org.assertj.core.api.Assertions.assertThat
import org.bazar.authorization.domain.role.RoleScope
import org.bazar.authorization.grpc.CreateUserRequest
import org.bazar.authorization.grpc.DeleteSpaceRequest
import org.bazar.authorization.grpc.DeleteUserRequest
import org.bazar.authorization.infrastructure.BaseGrpcTest
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

class SpaceAdminAuthorizationServiceTest : BaseGrpcTest() {

    companion object {
        private const val DEFAULT_USER_ROLE_ID = 1L
    }

    @Test
    @DisplayName("deleteUser should return PERMISSION_DENIED without DELETE permission")
    fun deleteUser_whenCallerHasNoDeletePermission() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, isCreator = false)
        initDataHelper.createSpaceUser(spaceId, targetUserId, DEFAULT_USER_ROLE_ID, isCreator = false)

        assertGrpcStatus(Status.PERMISSION_DENIED) {
            adminStub.deleteUser(
                DeleteUserRequest.newBuilder()
                    .setSpaceId(spaceId)
                    .setUserId(targetUserId.toString())
                    .build()
            )
        }
    }

    @Test
    @DisplayName("deleteUser should return SUCCESS and delete user data when target is in db")
    fun deleteUser_whenTargetInDb_successAndDeletesData() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()
        val customRoleId = initDataHelper.createRole(spaceId)

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, isCreator = true)
        initDataHelper.createSpaceUser(spaceId, targetUserId, customRoleId, isCreator = false)

        val response = adminStub.deleteUser(
            DeleteUserRequest.newBuilder()
                .setSpaceId(spaceId)
                .setUserId(targetUserId.toString())
                .build()
        )

        assertThat(response.success).isTrue()

        transaction {
            val usersInDb = initDataHelper.getAllSpaceUsers(spaceId)
            assertThat(usersInDb).noneMatch { it.userId == targetUserId && it.spaceId == spaceId }
        }
    }

    @Test
    @DisplayName("deleteUser should return SUCCESS when target user not in database")
    fun deleteUser_whenTargetNotInDb_success() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, isCreator = true)

        val response = adminStub.deleteUser(
            DeleteUserRequest.newBuilder()
                .setSpaceId(spaceId)
                .setUserId(targetUserId.toString())
                .build()
        )

        assertThat(response.success).isTrue()
    }

    @Test
    @DisplayName("deleteSpace should delete all users, USER scoped roles and mappings")
    fun deleteSpace_shouldDeleteSpaceUsersAndRoleData() = grpcTest {
        //given
        val spaceId = randomSpaceId()
        val actionsInDb = initDataHelper.getAllActions()
        val customRoleId = initDataHelper.createRole(spaceId)
        initDataHelper.createRolesActions(customRoleId, actionsInDb.first().id)

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = DEFAULT_USER_ROLE_ID, isCreator = true)
        initDataHelper.createSpaceUser(spaceId, UUID.randomUUID(), roleId = customRoleId, isCreator = false)

        val response = adminStub.deleteSpace(
            DeleteSpaceRequest.newBuilder()
                .setSpaceId(spaceId)
                .build()
        )

        assertThat(response.success).isTrue
        suspendTransaction {
            val usersInDb = initDataHelper.getAllSpaceUsers(spaceId)
            val rolesInDb = roleRepository.findAll()
            val roleIdsInDb = rolesInDb.map { it.id!! }
            val roleActionMappingsInDb = rolesActionsRepository.findAll()

            assertThat(usersInDb).hasSize(0)
                .noneMatch { it.spaceId == spaceId }

            assertThat(rolesInDb).isNotEmpty
                .allMatch { it.scope == RoleScope.GLOBAL }

            assertThat(roleActionMappingsInDb)
                .allMatch { roleIdsInDb.contains(it.roleId) }
        }
    }

    @Test
    @DisplayName("deleteSpace should return INVALID_ARGUMENT for empty request")
    fun deleteSpace_whenRequestIsEmpty() = grpcTest {
        assertGrpcStatus(Status.INVALID_ARGUMENT) {
            adminStub.deleteSpace(DeleteSpaceRequest.newBuilder().build())
        }
    }

    @Test
    @DisplayName("deleteSpace should return PERMISSION_DENIED without DELETE space permission")
    fun deleteSpace_whenCallerHasNoDeleteSpacePermission() = grpcTest {
        val spaceId = randomSpaceId()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, isCreator = false)

        assertGrpcStatus(Status.PERMISSION_DENIED) {
            adminStub.deleteSpace(
                DeleteSpaceRequest.newBuilder()
                    .setSpaceId(spaceId)
                    .build()
            )
        }
    }
}