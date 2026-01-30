package org.bazar.authorization.service

import org.assertj.core.api.Assertions.assertThat
import org.bazar.authorization.grpc.AuthorizeRequest
import org.bazar.authorization.infrastructure.BaseGrpcTest
import org.bazar.authorization.model.authz.enums.AuthorizationAction
import org.bazar.authorization.persistence.entity.UserSpaceRole
import org.bazar.authorization.persistence.entity.enums.Role.MEMBER
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SpaceAuthorizationServiceTest : BaseGrpcTest() {

    @Test
    @DisplayName("User has role member in space, should grant access")
    fun testAuthorize_shouldGrandAccess() {
        //given
        val authenticatedUserId = jwtTestSupplier.userId
        userSpaceRoleRepository.save(UserSpaceRole(1L, authenticatedUserId, MEMBER))
        //when
        val response = stub.authorize(
            AuthorizeRequest.newBuilder()
                .setKind("space")
                .setResourceId(1L)
                .setAction(AuthorizationAction.ACCESS_CHAT.actionName)
                .build()
        )
        //then
        assertThat(response.allowed).isTrue
    }

    @Test
    @DisplayName("User doesn't have role in space, should deny access")
    fun testAuthorize_shouldDenyAccess() {
        //when
        val response = stub.authorize(
            AuthorizeRequest.newBuilder()
                .setKind("space")
                .setResourceId(1L)
                .setAction(AuthorizationAction.ACCESS_CHAT.actionName)
                .build()
        )
        //then
        assertThat(response.allowed).isFalse
    }

    @Test
    @DisplayName("User has role CREATOR and tries to add more users, should deny access")
    fun testAuthorize_shouldDenyAccessNotEnoughRole() {
        //given
        val authenticatedUserId = jwtTestSupplier.userId
        userSpaceRoleRepository.save(UserSpaceRole(1L, authenticatedUserId, MEMBER))
        //when
        val response = stub.authorize(
            AuthorizeRequest.newBuilder()
                .setKind("space")
                .setResourceId(1L)
                .setAction(AuthorizationAction.ADD_USER_TO_SPACE.actionName)
                .build()
        )
        //then
        assertThat(response.allowed).isFalse
    }

}