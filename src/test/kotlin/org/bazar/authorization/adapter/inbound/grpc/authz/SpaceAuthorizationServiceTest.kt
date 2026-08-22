package org.bazar.authorization.adapter.inbound.grpc.authz

import io.grpc.Status
import io.mockk.coEvery
import org.assertj.core.api.Assertions.assertThat
import org.bazar.authorization.adapter.outbound.http.BazarSpaceHttpClient
import org.bazar.authorization.adapter.outbound.http.BazarSpaceUserResponse
import org.bazar.authorization.grpc.AuthorizeRequest
import org.bazar.authorization.infrastructure.BaseGrpcTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.koin.test.inject

class SpaceAuthorizationServiceTest : BaseGrpcTest() {

    private val bazarSpaceClientMock by inject<BazarSpaceHttpClient>()

    companion object {
        private const val DEFAULT_USER_ROLE_ID = 1L
        private const val READ_CHAT_MESSAGES_ACTION_NAME = "READ"
        private const val RESOURCE_CHAT_MESSAGES = "chat_messages"
    }

    @Test
    @DisplayName("authorize should return ALLOWED=true when user has the READ chat_messages action")
    fun authorize_whenUserHasAction_returnAllowed() = grpcTest {
        val spaceId = randomSpaceId()

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, isCreator = false)

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
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, initDataHelper.createRole(spaceId), isCreator = false)

        val allowed = stub.authorize(
            AuthorizeRequest.newBuilder()
                .setSpaceId(spaceId)
                .setResource(RESOURCE_CHAT_MESSAGES)
                .setAction(READ_CHAT_MESSAGES_ACTION_NAME)
                .build()
        )

        assertThat(allowed.allowed).isFalse
    }

    @Test
    @DisplayName("authorize should return PERMISSION_DENIED when user is not in the db for that space")
    fun authorize_whenUserNotInDb_returnPermissionDenied() = grpcTest {
        val spaceId = randomSpaceId()

        coEvery { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) } returns null

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
        val defaultRoleId = 1L

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, defaultRoleId, isCreator = true)

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
