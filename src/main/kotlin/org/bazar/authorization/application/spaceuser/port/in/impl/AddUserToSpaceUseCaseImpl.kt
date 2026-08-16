package org.bazar.authorization.application.spaceuser.port.`in`.impl

import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.application.spaceuser.command.AddUserToSpaceCommand
import org.bazar.authorization.application.spaceuser.port.`in`.AddUserToSpaceUseCase
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.authz.Permission
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException
import org.bazar.authorization.domain.spaceuser.SpaceUser
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class AddUserToSpaceUseCaseImpl(
    private val spaceUserRepositoryPort: SpaceUserRepositoryPort,
    private val authorizer: Authorizer
) : AddUserToSpaceUseCase {

    override suspend fun execute(command: AddUserToSpaceCommand) {
        if (!command.isCreator) {
            authorizer.authorizeOrThrow(
                buildAuthorizationCheck(
                    spaceId = command.spaceId,
                    userId = command.requesterId,
                    permission = Permission.SPACE_USER_ADD
                )
            )
        }

        suspendTransaction {
            spaceUserRepositoryPort.findBySpaceIdAndUserId(command.spaceId, command.userId)
                ?.let { throw DomainException(DomainErrors.USER_ALREADY_EXISTS) }

            spaceUserRepositoryPort.save(
                SpaceUser.create(command.spaceId, command.userId, AddUserToSpaceCommand.DEFAULT_ROLE_ID, command.isCreator)
            )
        }
    }
}
