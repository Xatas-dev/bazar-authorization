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
    @DisplayName("Should create user with default role = 'Дефолтыч'")
    fun createUser_shouldCreateMember() = grpcTest {
        //given
        val testSpaceId = randomSpaceId()
        val userIdToAdd = UUID.randomUUID()
        initDataHelper.createSpaceUser(testSpaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, isCreator = false)

        //when
        val response = adminStub.createUser(
            CreateUserRequest.newBuilder()
                .setSpaceId(testSpaceId)
                .setUserId(userIdToAdd.toString())
                .setIsCreator(false)
                .build()
        )

        //then
        val spaceUsers = initDataHelper.getAllSpaceUsers(testSpaceId)
            .filter { it.userId == userIdToAdd }
        assertThat(spaceUsers).hasSize(1)
        val createdUser = spaceUsers.first()
        assertThat(createdUser.spaceId).isEqualTo(testSpaceId)
        assertThat(createdUser.roleId).isEqualTo(DEFAULT_USER_ROLE_ID)
        assertThat(response.success).isTrue
    }

    @Test
    @DisplayName("Should create user with creator = true with role = 'Дефолтыч'")
    fun createUser_shouldCreateCreator() = grpcTest {
        //given
        val spaceId = randomSpaceId()
        val userIdToAdd = UUID.randomUUID()

        //when
        val response = adminStub.createUser(
            CreateUserRequest.newBuilder()
                .setSpaceId(spaceId)
                .setUserId(userIdToAdd.toString())
                .setIsCreator(true)
                .build()
        )

        //then
        assertThat(response.success).isTrue
        val createdUsers = initDataHelper.getAllSpaceUsers(spaceId)
        assertThat(createdUsers).hasSize(1)
        val createdUser = createdUsers.first()
        assertThat(createdUser.roleId).isEqualTo(DEFAULT_USER_ROLE_ID)
        assertThat(createdUser.spaceId).isEqualTo(spaceId)
    }

    @Test
    @DisplayName("createUser should return INVALID_ARGUMENT for empty request")
    fun createUser_whenRequestIsEmpty() = grpcTest {
        assertGrpcStatus(Status.INVALID_ARGUMENT) {
            adminStub.createUser(CreateUserRequest.newBuilder().build())
        }
    }

    @Test
    @DisplayName("createUser should return PERMISSION_DENIED when caller is not in space")
    fun createUser_whenCallerIsNotInSpace() = grpcTest {
        assertGrpcStatus(Status.PERMISSION_DENIED) {
            adminStub.createUser(
                CreateUserRequest.newBuilder()
                    .setSpaceId(randomSpaceId())
                    .setUserId(UUID.randomUUID().toString())
                    .setIsCreator(false)
                    .build()
            )
        }
    }

    @Test
    @DisplayName("createUser should return PERMISSION_DENIED when caller has no ADD action")
    fun createUser_whenCallerHasNoAddPermission() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, initDataHelper.createRole(spaceId), isCreator = false)

        assertGrpcStatus(Status.PERMISSION_DENIED) {
            adminStub.createUser(
                CreateUserRequest.newBuilder()
                    .setSpaceId(spaceId)
                    .setUserId(targetUserId.toString())
                    .setIsCreator(false)
                    .build()
            )
        }
    }

    @Test
    @DisplayName("createUser should return ALREADY_EXISTS when user is already in space")
    fun createUser_whenUserAlreadyExists() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, isCreator = true)
        initDataHelper.createSpaceUser(spaceId, targetUserId, DEFAULT_USER_ROLE_ID, isCreator = false)

        assertGrpcStatus(Status.ALREADY_EXISTS) {
            adminStub.createUser(
                CreateUserRequest.newBuilder()
                    .setSpaceId(spaceId)
                    .setUserId(targetUserId.toString())
                    .setIsCreator(false)
                    .build()
            )
        }
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
            val usersInDb = spaceUserRepository.findAll()
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
            val usersInDb = spaceUserRepository.findAll()
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