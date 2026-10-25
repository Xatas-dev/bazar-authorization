package org.bazar.authorization.application.role.port.`in`.impl

import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.EnrichedRole
import org.bazar.authorization.application.role.port.`in`.GetEnrichedRoleUseCase
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.role.query.GetRoleQuery
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.domain.authz.AuthorizationAction
import org.bazar.authorization.domain.exception.RoleNotFoundException
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class GetEnrichedRoleUseCaseImpl(
    private val roleRepositoryPort: RoleRepositoryPort,
    private val rolesActionsRepositoryPort: RolesActionsRepositoryPort,
    private val actionRepositoryPort: ActionRepositoryPort,
    private val authorizer: Authorizer
) : GetEnrichedRoleUseCase {

    override suspend fun execute(query: GetRoleQuery): EnrichedRole {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = query.spaceId,
                userId = query.requesterId,
                authorizationConstant = AuthorizationAction.ROLES_READ,
                resourceId = query.roleId.toString()
            )
        )

        return suspendTransaction {
            val role = roleRepositoryPort.findById(query.roleId)
                ?: throw RoleNotFoundException(query.roleId)
            val roleActionMappings = rolesActionsRepositoryPort.findAllByRoleId(query.roleId)
            val actions = actionRepositoryPort.findByIds(roleActionMappings.map { it.actionId })

            EnrichedRole(role, roleActionMappings, actions)
        }
    }
}
