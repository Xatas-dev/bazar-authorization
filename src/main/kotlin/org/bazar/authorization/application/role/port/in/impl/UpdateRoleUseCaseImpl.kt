package org.bazar.authorization.application.role.port.`in`.impl

import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.EnrichedRole
import org.bazar.authorization.application.role.command.UpdateRoleCommand
import org.bazar.authorization.application.role.port.`in`.UpdateRoleUseCase
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.domain.authz.AuthorizationAction
import org.bazar.authorization.domain.authz.PrincipalAttributes
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException
import org.bazar.authorization.domain.role.RoleActionMapping
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class UpdateRoleUseCaseImpl(
    private val roleRepositoryPort: RoleRepositoryPort,
    private val rolesActionsRepositoryPort: RolesActionsRepositoryPort,
    private val actionRepositoryPort: ActionRepositoryPort,
    private val authorizer: Authorizer
) : UpdateRoleUseCase {

    override suspend fun execute(command: UpdateRoleCommand): EnrichedRole {

        val (actionsFromDb, existingActionIds) = suspendTransaction {
            val actions = actionRepositoryPort.findByIds(command.actions.map { it.actionId })
            val existingIds = rolesActionsRepositoryPort.findAllByRoleId(command.roleId)
                .map { it.actionId }
                .toSet()
            actions to existingIds
        }

        val newlyGrantedActions = actionsFromDb.filter { it.id !in existingActionIds }

        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = command.spaceId,
                userId = command.requesterId,
                authorizationConstant = AuthorizationAction.ROLES_MANAGE,
                resourceId = command.roleId.toString(),
                principalAttributes = mapOf(
                    PrincipalAttributes.ACTIONS_TO_GRANT to newlyGrantedActions.map { "${it.resource}:${it.code}" }
                        .toString()
                )
            )
        )

        return suspendTransaction {
            val roleInDb = roleRepositoryPort.findById(command.roleId)
                ?: throw DomainException(DomainErrors.NO_SUCH_ROLE, "roleId: ${command.roleId}")

            val updatedRole = roleRepositoryPort.update(roleInDb.update(command.name, command.isVisible))

            val roleActionMappingsToCreate = command.actions.map {
                RoleActionMapping(
                    roleId = updatedRole.id!!,
                    actionId = it.actionId
                )
            }
            rolesActionsRepositoryPort.deleteAll(listOf(updatedRole.id!!))
            val createdRoleActionMappings = rolesActionsRepositoryPort.saveAll(roleActionMappingsToCreate)

            EnrichedRole(updatedRole, createdRoleActionMappings, actionsFromDb)
        }
    }
}
