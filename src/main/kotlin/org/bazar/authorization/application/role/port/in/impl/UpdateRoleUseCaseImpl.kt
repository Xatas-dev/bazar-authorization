package org.bazar.authorization.application.role.port.`in`.impl

import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.ActionDependencyResolver
import org.bazar.authorization.application.role.EnrichedRole
import org.bazar.authorization.application.role.command.UpdateRoleCommand
import org.bazar.authorization.application.role.port.`in`.UpdateRoleUseCase
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.authz.AuthorizationAction
import org.bazar.authorization.domain.authz.PrincipalAttributes
import org.bazar.authorization.domain.exception.RoleNotFoundException
import org.bazar.authorization.domain.role.RoleActionMapping
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class UpdateRoleUseCaseImpl(
    private val roleRepositoryPort: RoleRepositoryPort,
    private val rolesActionsRepositoryPort: RolesActionsRepositoryPort,
    private val actionRepositoryPort: ActionRepositoryPort,
    private val actionDependencyResolver: ActionDependencyResolver,
    private val authorizer: Authorizer
) : UpdateRoleUseCase {

    override suspend fun execute(command: UpdateRoleCommand): EnrichedRole {

        val (role, allActions, existingActionIds) = suspendTransaction {
            val r = roleRepositoryPort.findById(command.roleId)
                ?: throw RoleNotFoundException(command.roleId)
            val all = actionRepositoryPort.findAll()
            val existing = rolesActionsRepositoryPort
                .findAllByRoleId(command.roleId)
                .map { it.actionId }
                .toSet()
            Triple(r, all, existing)
        }

        val requestedActionIds = command.actions.map { it.actionId }.toSet()
        val requestedActions = allActions.filter { it.id in requestedActionIds }
        val existingActions = allActions.filter { it.id in existingActionIds }

        val actionDiff = computeActionDiff(requestedActions, existingActions)

        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = command.spaceId,
                userId = command.requesterId,
                authorizationConstant = AuthorizationAction.ROLES_MANAGE,
                resourceId = command.roleId.toString(),
                principalAttributes = mapOf(
                    PrincipalAttributes.ACTIONS_TO_GRANT to toAuthzString(actionDiff)
                )
            )
        )

        actionDependencyResolver.validateActionsDependency(requestedActions)

        return suspendTransaction {
            val updatedRole = roleRepositoryPort.update(
                role.update(command.name, command.isVisible)
            )

            val mappings = requestedActions.map {
                RoleActionMapping(roleId = updatedRole.id!!, actionId = it.id)
            }

            rolesActionsRepositoryPort.deleteAll(listOf(updatedRole.id!!))
            val savedMappings = rolesActionsRepositoryPort.saveAll(mappings)

            EnrichedRole(updatedRole, savedMappings, requestedActions)
        }
    }

    private fun computeActionDiff(
        requested: List<Action>,
        existing: List<Action>
    ): List<Action> {
        val existingIds = existing.mapTo(HashSet()) { it.id }
        val requestedIds = requested.mapTo(HashSet()) { it.id }

        val granted = requested.filter { it.id !in existingIds }
        val revoked = existing.filter { it.id !in requestedIds }
        return granted + revoked
    }

    private fun toAuthzString(actions: List<Action>): String =
        actions.map { "${it.resource}:${it.code}" }.toString()
}

