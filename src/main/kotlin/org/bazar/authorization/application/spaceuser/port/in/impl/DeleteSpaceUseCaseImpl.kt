package org.bazar.authorization.application.spaceuser.port.`in`.impl

import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.application.spaceuser.command.DeleteSpaceCommand
import org.bazar.authorization.application.spaceuser.port.`in`.DeleteSpaceUseCase
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.authz.Permission
import org.bazar.authorization.domain.role.RoleScope
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class DeleteSpaceUseCaseImpl(
    private val spaceUserRepositoryPort: SpaceUserRepositoryPort,
    private val roleRepositoryPort: RoleRepositoryPort,
    private val rolesActionsRepositoryPort: RolesActionsRepositoryPort,
    private val authorizer: Authorizer
) : DeleteSpaceUseCase {

    override suspend fun execute(command: DeleteSpaceCommand) {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = command.spaceId,
                userId = command.requesterId,
                permission = Permission.SPACE_DELETE
            )
        )

        suspendTransaction {
            val roleIds = spaceUserRepositoryPort.findAllRoleIdsBySpaceId(command.spaceId)
            spaceUserRepositoryPort.deleteUsersBySpaceId(command.spaceId)

            val roleIdsToDelete = roleRepositoryPort
                .findByScopeAndIdsIn(RoleScope.SPACE, roleIds)
                .map { it.id!! }
            rolesActionsRepositoryPort.deleteAll(roleIdsToDelete)
            roleRepositoryPort.deleteAll(roleIdsToDelete)
        }
    }
}
