package org.bazar.authorization.service

import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.bazar.authorization.grpc.AddUserToSpaceRequest
import org.bazar.authorization.grpc.CreateSpaceRequest
import org.bazar.authorization.grpc.RemoveUserFromSpaceRequest
import org.bazar.authorization.infrastructure.BaseGrpcTest
import org.bazar.authorization.persistence.entity.UserSpaceRole
import org.bazar.authorization.persistence.entity.UserSpaceRoleId
import org.bazar.authorization.persistence.entity.enums.Role
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.test.context.support.WithMockUser
import java.util.*

@WithMockUser
class SpaceAdminAuthorizationServiceTest : BaseGrpcTest() {

    @Test
    @DisplayName("Adding user to space should save user space role and return true")
    fun addUserRoleToSpace_shouldSaveAndReturnTrue() {
        //given
        val authenticatedUserId = jwtTestSupplier.userId
        val userToBeAdded = UUID.randomUUID()
        userSpaceRoleRepository.save(UserSpaceRole(UserSpaceRoleId(2L, authenticatedUserId), Role.CREATOR))
        //when
        val response = adminStub.addUserToSpace(
            AddUserToSpaceRequest.newBuilder().setSpaceId(2L).setUserId(userToBeAdded.toString())
                .setRole(Role.MEMBER.name).build()
        )
        //then
        val allUsers = userSpaceRoleRepository.findAll()
        assertThat(allUsers)
            .hasSize(2)
            .extracting(UserSpaceRole::id, UserSpaceRole::role)
            .containsExactlyInAnyOrder(
                Tuple(UserSpaceRoleId(2L, authenticatedUserId), Role.CREATOR),
                Tuple(UserSpaceRoleId(2L, userToBeAdded), Role.MEMBER)
            )

        assertEquals(true, response.success)
    }

    @Test
    @DisplayName("should throw insufficient permissions for the action")
    fun addUserRoleToSpace_shouldThrowInsufficientPermissions() {
        //given
        val userToBeAdded = UUID.randomUUID()
        val authenticatedUserId = jwtTestSupplier.userId
        userSpaceRoleRepository.save(UserSpaceRole(UserSpaceRoleId(2L, authenticatedUserId), Role.MEMBER))
        //when
        val error = assertThrows<StatusRuntimeException> { adminStub.addUserToSpace(
            AddUserToSpaceRequest.newBuilder().setSpaceId(2L).setUserId(userToBeAdded.toString())
                .setRole(Role.MEMBER.name).build()) }
        //then
        assertThat(error.status.code).isEqualTo(Status.PERMISSION_DENIED.code)
    }

    @Test
    @DisplayName("Should send 400 status for empty request")
    fun addUserRoleToSpace_whenRequestIsEmpty() {
        assertThrows<StatusRuntimeException> {
            adminStub.addUserToSpace(AddUserToSpaceRequest.newBuilder().build())
        }.status == Status.INVALID_ARGUMENT
    }

    @Test
    @DisplayName("should remove user role from space")
    fun removeUserFromSpace_shouldReturnTrue() {
        //given
        val authenticatedUserId = jwtTestSupplier.userId
        val userToBeRemovedId = UUID.randomUUID()
        userSpaceRoleRepository.save(UserSpaceRole(UserSpaceRoleId(1L, authenticatedUserId), Role.CREATOR))
        userSpaceRoleRepository.save(UserSpaceRole(UserSpaceRoleId(1L, userToBeRemovedId), Role.MEMBER))
        //when
        val response = adminStub.removeUserFromSpace(
            RemoveUserFromSpaceRequest.newBuilder()
                .setSpaceId(1L)
                .setUserId(userToBeRemovedId.toString())
                .build()
        )
        val all = userSpaceRoleRepository.findAll()
        assertTrue { response.success }
        assertThat(all)
            .hasSize(1)
    }

    @Test
    @DisplayName("should throw when empty request message")
    fun removeUserFromSpace_whenEmptyRequest() {
        assertThrows<StatusRuntimeException> {
            adminStub.removeUserFromSpace(RemoveUserFromSpaceRequest.newBuilder().build())
        }.status == Status.INVALID_ARGUMENT
    }

    @Test
    @DisplayName("should create an owner in a space")
    fun createSpace_shouldCreateNewUserWithRoleCreator() {
        //given
        val authenticatedUserId = jwtTestSupplier.userId
        //when
        val response = adminStub.createSpace(CreateSpaceRequest.newBuilder().setSpaceId(1L).build())
        //then
        val allRolesInDb = userSpaceRoleRepository.findAll()
        assertThat(allRolesInDb)
            .hasSize(1)
            .extracting(UserSpaceRole::id, UserSpaceRole::role)
            .containsExactlyInAnyOrder(
                Tuple(UserSpaceRoleId(1L, authenticatedUserId), Role.CREATOR)
            )

        assertEquals(true, response.success)
    }

    @Test
    @DisplayName("should throw if creator already exists")
    fun createSpace_shouldThrowIfCreatorExists() {
        //given
        val authenticatedUserId = jwtTestSupplier.userId
        userSpaceRoleRepository.save(UserSpaceRole(UserSpaceRoleId(1L, authenticatedUserId), Role.CREATOR))
        //when
        val error = assertThrows<StatusRuntimeException> {
            adminStub.createSpace(CreateSpaceRequest.newBuilder().setSpaceId(1L).build())
        }
        //then
        assertThat(error.status.code).isEqualTo(Status.INVALID_ARGUMENT.code)
    }

}