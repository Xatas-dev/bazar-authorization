package org.bazar.authorization.application.role.port.`in`.impl

import org.bazar.authorization.application.role.port.`in`.GetRolesInSpaceUseCase
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.query.GetRolesQuery
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.domain.authz.Permission
import org.bazar.authorization.domain.role.Role
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class GetRolesInSpaceUseCaseImpl(
    private val roleRepositoryPort: RoleRepositoryPort,
    private val authorizer: Authorizer
) : GetRolesInSpaceUseCase {

    override suspend fun execute(query: GetRolesQuery): List<Role> {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = query.spaceId,
                userId = query.requesterId,
                permission = Permission.ROLES_READ
            )
        )

        return suspendTransaction {
            roleRepositoryPort.findAllBySpaceId(query.spaceId)
        }
    }
}
