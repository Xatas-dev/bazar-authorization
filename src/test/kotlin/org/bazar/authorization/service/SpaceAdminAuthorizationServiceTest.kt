package org.bazar.authorization.service

import io.grpc.Status
import org.assertj.core.api.Assertions.assertThat
import org.bazar.authorization.database.entity.enums.RoleScope
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
        private const val CREATOR_ROLE_ID = 1L
        private const val DEFAULT_USER_ROLE_ID = 2L
    }

    @Test
    @DisplayName("Should create user with default role = 'Лил непищик' when creator=false")
    fun createUser_shouldCreateMember() = grpcTest {
        //given
        val testSpaceId = randomSpaceId()
        val userIdToAdd = UUID.randomUUID()
        initDataHelper.createSpaceUser(testSpaceId, authenticatedUserId, CREATOR_ROLE_ID, creator = true)

        //when
        val response = adminStub.createUser(
            CreateUserRequest.newBuilder()
                .setSpaceId(testSpaceId)
                .setUserId(userIdToAdd.toString())
                .setCreator(false)
                .build()
        )

        //then
        val spaceUsers = initDataHelper.getAllSpaceUsers(testSpaceId)
            .filter { it.userId == userIdToAdd }
        assertThat(spaceUsers).hasSize(1)
        val createdUser = spaceUsers.first()
        assertThat(createdUser.spaceId).isEqualTo(testSpaceId)
        assertThat(createdUser.creator).isFalse
        assertThat(createdUser.roleId).isEqualTo(DEFAULT_USER_ROLE_ID)
        assertThat(response.success).isTrue
    }

    @Test
    @DisplayName("Should create user with creator = true with role = 'Создатель'")
    fun createUser_shouldCreateCreator() = grpcTest {
        //given
        val spaceId = randomSpaceId()
        val userIdToAdd = UUID.randomUUID()

        //when
        val response = adminStub.createUser(
            CreateUserRequest.newBuilder()
                .setSpaceId(spaceId)
                .setUserId(userIdToAdd.toString())
                .setCreator(true)
                .build()
        )

        //then
        assertThat(response.success).isTrue
        val createdUsers = initDataHelper.getAllSpaceUsers(spaceId)
        assertThat(createdUsers).hasSize(1)
        val createdUser = createdUsers.first()
        assertThat(createdUser.roleId).isEqualTo(CREATOR_ROLE_ID)
        assertThat(createdUser.spaceId).isEqualTo(spaceId)
        assertThat(createdUser.creator).isTrue
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
                    .setCreator(false)
                    .build()
            )
        }
    }

    @Test
    @DisplayName("createUser should return PERMISSION_DENIED when caller has no ADD action")
    fun createUser_whenCallerHasNoAddPermission() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, initDataHelper.createRole(), creator = false)

        assertGrpcStatus(Status.PERMISSION_DENIED) {
            adminStub.createUser(
                CreateUserRequest.newBuilder()
                    .setSpaceId(spaceId)
                    .setUserId(targetUserId.toString())
                    .setCreator(false)
                    .build()
            )
        }
    }

    @Test
    @DisplayName("createUser should return ALREADY_EXISTS when user is already in space")
    fun createUser_whenUserAlreadyExists() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, CREATOR_ROLE_ID, creator = true)
        initDataHelper.createSpaceUser(spaceId, targetUserId, DEFAULT_USER_ROLE_ID, creator = false)

        assertGrpcStatus(Status.ALREADY_EXISTS) {
            adminStub.createUser(
                CreateUserRequest.newBuilder()
                    .setSpaceId(spaceId)
                    .setUserId(targetUserId.toString())
                    .setCreator(false)
                    .build()
            )
        }
    }

    @Test
    @DisplayName("deleteUser should return PERMISSION_DENIED without DELETE permission")
    fun deleteUser_whenCallerHasNoDeletePermission() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, creator = false)
        initDataHelper.createSpaceUser(spaceId, targetUserId, DEFAULT_USER_ROLE_ID, creator = false)

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
    @DisplayName("deleteUser should return SUCCESS and delete user data and scope USER roles when target is in db")
    fun deleteUser_whenTargetInDb_successAndDeletesData() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()
        val customRoleId = initDataHelper.createRole()

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, CREATOR_ROLE_ID, creator = true)
        initDataHelper.createSpaceUser(spaceId, targetUserId, customRoleId, creator = false)

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

            val rolesInDb = roleRepository.getAllRoles()
            assertThat(rolesInDb).noneMatch { it.id == customRoleId }
        }
    }

    @Test
    @DisplayName("deleteUser should return SUCCESS when target user not in database")
    fun deleteUser_whenTargetNotInDb_success() = grpcTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.randomUUID()

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, CREATOR_ROLE_ID, creator = true)

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
        val customRoleId = initDataHelper.createRole()
        initDataHelper.createRolesActions(customRoleId, actionsInDb.first().id)

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = CREATOR_ROLE_ID, creator = true)
        initDataHelper.createSpaceUser(spaceId, UUID.randomUUID(), roleId = customRoleId, creator = false)

        val response = adminStub.deleteSpace(
            DeleteSpaceRequest.newBuilder()
                .setSpaceId(spaceId)
                .build()
        )

        assertThat(response.success).isTrue
        suspendTransaction {
            val usersInDb = spaceUserRepository.findAll()
            val rolesInDb = roleRepository.getAllRoles()
            val roleIdsInDb = rolesInDb.map { it.id!! }
            val roleActionMappingsInDb = rolesActionsRepository.getAllRoleActionMappings()

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
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, creator = false)

        assertGrpcStatus(Status.PERMISSION_DENIED) {
            adminStub.deleteSpace(
                DeleteSpaceRequest.newBuilder()
                    .setSpaceId(spaceId)
                    .build()
            )
        }
    }
}