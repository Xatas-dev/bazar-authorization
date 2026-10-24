package org.bazar.authorization.application.role.port.`in`.impl

import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.EnrichedRole
import org.bazar.authorization.application.role.command.CreateRoleCommand
import org.bazar.authorization.application.role.port.`in`.CreateRoleUseCase
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.authz.AuthorizationAction.ROLES_MANAGE
import org.bazar.authorization.domain.authz.PrincipalAttributes
import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.domain.role.RoleActionMapping
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class CreateRoleUseCaseImpl(
    private val roleRepositoryPort: RoleRepositoryPort,
    private val rolesActionsRepositoryPort: RolesActionsRepositoryPort,
    private val actionRepositoryPort: ActionRepositoryPort,
    private val authorizer: Authorizer
) : CreateRoleUseCase {

    override suspend fun execute(command: CreateRoleCommand): EnrichedRole {
        val actionsFromDb = suspendTransaction {
            actionRepositoryPort.findByIds(command.actions.map { it.actionId })
        }

        authorize(command, actionsFromDb)

        return suspendTransaction {
            val createdRole = roleRepositoryPort.save(
                Role.createSpaceScoped(command.name, command.isVisible, command.createdBy, command.spaceId)
            )
            val roleId = createdRole.id!!

            val createdMappings = rolesActionsRepositoryPort.saveAll(
                command.actions.map {
                    RoleActionMapping(
                        roleId = roleId,
                        actionId = it.actionId
                    )
                }
            )

            EnrichedRole(createdRole, createdMappings, actionsFromDb)
        }
    }

    private suspend fun authorize(
        command: CreateRoleCommand,
        actionsFromDb: List<Action>
    ) {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = command.spaceId,
                userId = command.createdBy,
                authorizationConstant = ROLES_MANAGE,
                principalAttributes = mapOf(
                    PrincipalAttributes.ACTIONS_TO_GRANT to
                            actionsFromDb.map { "${it.resource}:${it.code}" }.toString()
                )
            )
        )
    }
}
