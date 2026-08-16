package org.bazar.authorization.application.spaceuser.port.`in`.impl

import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.application.spaceuser.command.AssignRoleCommand
import org.bazar.authorization.application.spaceuser.port.`in`.AssignRoleToUserUseCase
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.authz.Permission
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class AssignRoleToUserUseCaseImpl(
    private val spaceUserRepositoryPort: SpaceUserRepositoryPort,
    private val authorizer: Authorizer
) : AssignRoleToUserUseCase {

    override suspend fun execute(command: AssignRoleCommand) {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = command.spaceId,
                userId = command.requesterId,
                permission = Permission.ROLES_ASSIGN,
                resourceId = command.roleId.toString()
            )
        )

        suspendTransaction {
            val existingUser = spaceUserRepositoryPort.findBySpaceIdAndUserId(command.spaceId, command.targetUserId)
                ?: throw DomainException(
                    DomainErrors.NO_SUCH_USER_IN_SPACE,
                    "userId: ${command.targetUserId}, spaceId: ${command.spaceId}"
                )

            spaceUserRepositoryPort.save(existingUser.withRole(command.roleId))
        }
    }
}
