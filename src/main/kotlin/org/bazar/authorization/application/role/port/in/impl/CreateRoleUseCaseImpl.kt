package org.bazar.authorization.application.role.port.`in`.impl

import org.bazar.authorization.application.action.port.out.ActionAttributeRepositoryPort
import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.command.CreateRoleCommand
import org.bazar.authorization.application.role.port.`in`.CreateRoleUseCase
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.role.resolveActionAttributes
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.domain.authz.PrincipalAttributes
import org.bazar.authorization.domain.authz.Permission
import org.bazar.authorization.application.role.EnrichedRole
import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.application.role.RoleActionMapping
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class CreateRoleUseCaseImpl(
    private val roleRepositoryPort: RoleRepositoryPort,
    private val rolesActionsRepositoryPort: RolesActionsRepositoryPort,
    private val actionRepositoryPort: ActionRepositoryPort,
    private val actionAttributeRepositoryPort: ActionAttributeRepositoryPort,
    private val authorizer: Authorizer
) : CreateRoleUseCase {

    override suspend fun execute(command: CreateRoleCommand): EnrichedRole {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = command.spaceId,
                userId = command.createdBy,
                permission = Permission.ROLES_CREATE,
                principalAttributes = mapOf(
                    PrincipalAttributes.ACTIONS_TO_GRANT to command.actions.map { it.actionId }.toString()
                )
            )
        )

        return suspendTransaction {
            val attributeEntities = actionAttributeRepositoryPort.findByIds(
                command.actions.flatMap { it.attributes.map { attr -> attr.attributeId } }
            )

            val actionIdToAttributes = resolveActionAttributes(command.actions, attributeEntities)

            val createdRole = roleRepositoryPort.save(
                Role.createSpaceScoped(command.name, command.isVisible, command.createdBy, command.spaceId)
            )

            val roleActionMappingsToCreate = command.actions.map {
                RoleActionMapping(
                    roleId = createdRole.id!!,
                    actionId = it.actionId,
                    assignedAttributes = actionIdToAttributes[it.actionId]
                )
            }

            val createdRoleActionMappings = rolesActionsRepositoryPort.saveAll(roleActionMappingsToCreate)
            val actions = actionRepositoryPort.findByIds(command.actions.map { it.actionId })

            EnrichedRole(createdRole, createdRoleActionMappings, actions, attributeEntities)
        }
    }
}
