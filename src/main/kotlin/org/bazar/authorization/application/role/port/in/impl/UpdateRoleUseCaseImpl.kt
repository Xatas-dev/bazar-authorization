package org.bazar.authorization.application.role.port.`in`.impl

import org.bazar.authorization.application.action.port.out.ActionAttributeRepositoryPort
import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.command.UpdateRoleCommand
import org.bazar.authorization.application.role.port.`in`.UpdateRoleUseCase
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.role.port.out.RolesActionsRepositoryPort
import org.bazar.authorization.application.role.resolveActionAttributes
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.domain.authz.PrincipalAttributes
import org.bazar.authorization.domain.authz.Permission
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException
import org.bazar.authorization.application.role.EnrichedRole
import org.bazar.authorization.application.role.RoleActionMapping
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class UpdateRoleUseCaseImpl(
    private val roleRepositoryPort: RoleRepositoryPort,
    private val rolesActionsRepositoryPort: RolesActionsRepositoryPort,
    private val actionRepositoryPort: ActionRepositoryPort,
    private val actionAttributeRepositoryPort: ActionAttributeRepositoryPort,
    private val authorizer: Authorizer
) : UpdateRoleUseCase {

    override suspend fun execute(command: UpdateRoleCommand): EnrichedRole {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = command.spaceId,
                userId = command.requesterId,
                permission = Permission.ROLES_EDIT,
                resourceId = command.roleId.toString(),
                principalAttributes = mapOf(
                    PrincipalAttributes.ACTIONS_TO_GRANT to command.actions.map { it.actionId }.toString()
                )
            )
        )

        return suspendTransaction {
            val roleInDb = roleRepositoryPort.findById(command.roleId)
                ?: throw DomainException(DomainErrors.NO_SUCH_ROLE, "roleId: ${command.roleId}")

            val attributeEntities = actionAttributeRepositoryPort.findByIds(
                command.actions.flatMap { it.attributes.map { attr -> attr.attributeId } }
            )

            val actionIdToAttributes = resolveActionAttributes(command.actions, attributeEntities)

            val updatedRole = roleRepositoryPort.update(roleInDb.update(command.name, command.isVisible))

            val roleActionMappingsToCreate = command.actions.map {
                RoleActionMapping(
                    roleId = updatedRole.id!!,
                    actionId = it.actionId,
                    assignedAttributes = actionIdToAttributes[it.actionId]
                )
            }
            rolesActionsRepositoryPort.deleteAll(listOf(updatedRole.id!!))
            val createdRoleActionMappings = rolesActionsRepositoryPort.saveAll(roleActionMappingsToCreate)
            val actions = actionRepositoryPort.findByIds(command.actions.map { it.actionId })

            EnrichedRole(updatedRole, createdRoleActionMappings, actions, attributeEntities)
        }
    }
}
