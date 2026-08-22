package org.bazar.authorization.adapter.inbound.grpc.spaceuser

import org.bazar.authorization.adapter.inbound.grpc.validate
import org.bazar.authorization.application.shared.AuthenticatedUserProvider
import org.bazar.authorization.application.spaceuser.command.DeleteSpaceCommand
import org.bazar.authorization.application.spaceuser.command.DeleteUserFromSpaceCommand
import org.bazar.authorization.application.spaceuser.port.`in`.DeleteSpaceUseCase
import org.bazar.authorization.application.spaceuser.port.`in`.DeleteUserFromSpaceUseCase
import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpcKt.AuthorizationAdminServiceCoroutineImplBase
import org.bazar.authorization.grpc.CreateUserRequest
import org.bazar.authorization.grpc.CreateUserResponse
import org.bazar.authorization.grpc.DeleteSpaceRequest
import org.bazar.authorization.grpc.DeleteSpaceResponse
import org.bazar.authorization.grpc.DeleteUserRequest
import org.bazar.authorization.grpc.DeleteUserResponse
import org.bazar.authorization.infrastructure.util.logger
import java.util.UUID

class SpaceAdminAuthorizationService(
    private val deleteSpaceUseCase: DeleteSpaceUseCase,
    private val deleteUserFromSpaceUseCase: DeleteUserFromSpaceUseCase,
    private val authenticatedUserProvider: AuthenticatedUserProvider
) : AuthorizationAdminServiceCoroutineImplBase() {

    private val logger = logger()

    override suspend fun deleteSpace(request: DeleteSpaceRequest): DeleteSpaceResponse {
        request.validate()
        val authenticatedUserId = authenticatedUserProvider.getUserId()

        deleteSpaceUseCase.execute(DeleteSpaceCommand(request.spaceId, authenticatedUserId))

        return DeleteSpaceResponse.newBuilder().setSuccess(true).build()
    }

    override suspend fun createUser(request: CreateUserRequest): CreateUserResponse {
        logger.info("Create user admin request is deprecated and disabled")

        return CreateUserResponse.newBuilder().setSuccess(true).build()
    }

    override suspend fun deleteUser(request: DeleteUserRequest): DeleteUserResponse {
        request.validate()
        val authenticatedUserId = authenticatedUserProvider.getUserId()

        deleteUserFromSpaceUseCase.execute(
            DeleteUserFromSpaceCommand(
                spaceId = request.spaceId,
                requesterId = authenticatedUserId,
                userId = UUID.fromString(request.userId),
                isCreator = request.isCreator
            )
        )

        return DeleteUserResponse.newBuilder().setSuccess(true).build()
    }
}
