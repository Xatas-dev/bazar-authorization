package org.bazar.authorization.application.spaceuser.port.`in`.impl

import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.application.spaceuser.command.DeleteUserFromSpaceCommand
import org.bazar.authorization.application.spaceuser.port.`in`.DeleteUserFromSpaceUseCase
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.authz.Permission
import org.bazar.authorization.domain.authz.ResourceAttributes
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class DeleteUserFromSpaceUseCaseImpl(
    private val spaceUserRepositoryPort: SpaceUserRepositoryPort,
    private val authorizer: Authorizer
) : DeleteUserFromSpaceUseCase {

    override suspend fun execute(command: DeleteUserFromSpaceCommand) {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = command.spaceId,
                userId = command.requesterId,
                permission = Permission.SPACE_USER_DELETE,
                resourceAttributes = mapOf(ResourceAttributes.IS_CREATOR to command.isCreator.toString())
            )
        )

        suspendTransaction {
            spaceUserRepositoryPort.deleteBySpaceIdAndUserId(command.spaceId, command.userId)
        }
    }
}
