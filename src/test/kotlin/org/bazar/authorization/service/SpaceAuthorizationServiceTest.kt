package org.bazar.authorization.service

import io.grpc.Status
import org.assertj.core.api.Assertions.assertThat
import org.bazar.authorization.grpc.AuthorizeRequest
import org.bazar.authorization.infrastructure.BaseGrpcTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SpaceAuthorizationServiceTest : BaseGrpcTest() {

    companion object {
        private const val DEFAULT_USER_ROLE_ID = 2L
        private const val READ_CHAT_MESSAGES_ACTION_NAME = "READ"
        private const val RESOURCE_CHAT_MESSAGES = "chat_messages"
    }

    @Test
    @DisplayName("authorize should return ALLOWED=true when user has the READ chat_messages action")
    fun authorize_whenUserHasAction_returnAllowed() = grpcTest {
        val spaceId = randomSpaceId()

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID)

        val response = stub.authorize(
            AuthorizeRequest.newBuilder()
                .setSpaceId(spaceId)
                .setResource(RESOURCE_CHAT_MESSAGES)
                .setAction(READ_CHAT_MESSAGES_ACTION_NAME)
                .build()
        )

        assertThat(response.allowed).isTrue()
    }

    @Test
    @DisplayName("authorize should return PERMISSION_DENIED when user is in space but lacks the specific action")
    fun authorize_whenUserLacksAction_returnPermissionDenied() = grpcTest {
        val spaceId = randomSpaceId()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, initDataHelper.createRole())

        assertGrpcStatus(Status.PERMISSION_DENIED) {
            stub.authorize(
                AuthorizeRequest.newBuilder()
                    .setSpaceId(spaceId)
                    .setResource(RESOURCE_CHAT_MESSAGES)
                    .setAction(READ_CHAT_MESSAGES_ACTION_NAME)
                    .build()
            )
        }
    }

    @Test
    @DisplayName("authorize should return PERMISSION_DENIED when user is not in the db for that space")
    fun authorize_whenUserNotInDb_returnPermissionDenied() = grpcTest {
        val spaceId = randomSpaceId()

        assertGrpcStatus(Status.PERMISSION_DENIED) {
            stub.authorize(
                AuthorizeRequest.newBuilder()
                    .setSpaceId(spaceId)
                    .setResource(RESOURCE_CHAT_MESSAGES)
                    .setAction(READ_CHAT_MESSAGES_ACTION_NAME)
                    .build()
            )
        }
    }

    @Test
    @DisplayName("authorize should return ALLOWED=true when space creator tries to WRITE chat messages")
    fun authorize_creatorWriteChatMessages_returnAllowed() = grpcTest {
        val spaceId = randomSpaceId()
        val creatorRoleId = 1L

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, creatorRoleId)

        val response = stub.authorize(
            AuthorizeRequest.newBuilder()
                .setSpaceId(spaceId)
                .setResource(RESOURCE_CHAT_MESSAGES)
                .setAction("WRITE")
                .build()
        )

        assertThat(response.allowed).isTrue()
    }
}
