package org.bazar.authorization.service

import org.bazar.authorization.database.entity.RoleEntity
import org.bazar.authorization.database.entity.RoleWithActionMappings
import org.bazar.authorization.database.entity.RolesActionsEntity
import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.database.repository.RoleRepository
import org.bazar.authorization.database.repository.RolesActionsRepository
import org.bazar.authorization.utils.buildRole
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
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

    suspend fun getRoleWithActionMappings(roleId: Long): RoleWithActionMappings  = suspendTransaction {
        val role = roleRepository.findById(roleId) ?: throw ApiException(ApiExceptions.NO_SUCH_ROLE, "roleId: $roleId")
        val rolesActions =  rolesActionsRepository.findAllByRoleId(roleId)
        role.toRoleWithActionMappings(rolesActions)
    }

    suspend fun createUserScopedRole(actionsWithAttributes: Map<Int, Map<String, String>?>) = suspendTransaction {
        val createdRole = roleRepository.save(buildRole(RoleScope.USER, "Custom"))

        val roleActionMappingsToCreate = actionsWithAttributes.map {
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

}