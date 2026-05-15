package org.bazar.authorization.service

import org.bazar.authorization.database.entity.RoleWithActionMappings
import org.bazar.authorization.database.entity.RolesActionsEntity
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.database.repository.RoleRepository
import org.bazar.authorization.database.repository.RolesActionsRepository
import org.bazar.authorization.model.commands.CreateRoleCommand
import org.bazar.authorization.model.commands.UpdateRoleCommand
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.mapper.toRoleEntity
import org.bazar.authorization.utils.extensions.toRoleWithActionMappings
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class RoleService(
    private val rolesActionsRepository: RolesActionsRepository,
    private val roleRepository: RoleRepository
) {

    suspend fun deleteAllByRoleIdsAndScope(roleIds: List<Long>, scope: RoleScope) = suspendTransaction {
        val roleIdsToDelete = roleRepository.getAllByScopeAndRoleIdsIn(scope, roleIds).map { it.id!! }
        rolesActionsRepository.deleteAll(roleIdsToDelete)
        roleRepository.deleteAll(roleIdsToDelete)
    }

    suspend fun getRoleActionMappings(roleId: Long, actionId: Int) = suspendTransaction {
        rolesActionsRepository.findByRoleIdAndActionId(roleId, actionId)
            ?: throw ApiException(
                ApiExceptions.NO_SUCH_ACTION_IN_ROLE,
                "roleId: $roleId, not found actionId: $actionId"
            )
    }

    suspend fun getRoleActionMappings(roleId: Long) = suspendTransaction {
        rolesActionsRepository.findAllByRoleId(roleId)
    }

    suspend fun getRoleWithActionMappings(roleId: Long): RoleWithActionMappings = suspendTransaction {
        val role = roleRepository.findById(roleId) ?: throw ApiException(ApiExceptions.NO_SUCH_ROLE, "roleId: $roleId")
        val rolesActions = rolesActionsRepository.findAllByRoleId(roleId)
        role.toRoleWithActionMappings(rolesActions)
    }

    suspend fun createSpaceScopedRole(createRoleCommand: CreateRoleCommand) = suspendTransaction {
        val createdRole = roleRepository.save(createRoleCommand.toRoleEntity())

        val roleActionMappingsToCreate = createRoleCommand.actionIdToAttributes.map {
            RolesActionsEntity(
                createdRole.id!!,
                it.key,
                it.value,
            )
        }

        val createdRoleActionMappings = rolesActionsRepository.saveAll(roleActionMappingsToCreate)
        createdRole.toRoleWithActionMappings(createdRoleActionMappings)
    }

    suspend fun getAllRolesByIds(roleIds: Collection<Long>) = suspendTransaction {
        roleRepository.getAllByRoleIdsIn(roleIds.distinct())
    }

    suspend fun getRoleById(roleId: Long) = suspendTransaction {
        roleRepository.findById(roleId) ?: throw ApiException(ApiExceptions.NO_SUCH_ROLE, "roleId: $roleId")
    }

    suspend fun updateRole(command: UpdateRoleCommand) = suspendTransaction {
        val roleInDb = roleRepository.findById(command.roleId) ?: throw ApiException(
            ApiExceptions.NO_SUCH_ROLE,
            "roleId: ${command.roleId}"
        )

        val updatedRole = roleRepository.update(command.toRoleEntity(roleInDb))

        val roleActionMappingsToCreate = command.actionIdToAttributes.map {
            RolesActionsEntity(
                updatedRole.id!!,
                it.key,
                it.value,
            )
        }

        val createdRoleActionMappings = rolesActionsRepository.saveAll(roleActionMappingsToCreate)

        updatedRole.toRoleWithActionMappings(createdRoleActionMappings)
    }

}