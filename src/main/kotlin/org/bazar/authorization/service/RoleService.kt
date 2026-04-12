package org.bazar.authorization.service

import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.database.repository.RoleRepository
import org.bazar.authorization.database.repository.RolesActionsRepository
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
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

    suspend fun getRoleWithActionAndAttributesOrThrow(roleId: Long, actionId: Int) = suspendTransaction {
        rolesActionsRepository.findByRoleIdAndActionId(roleId, actionId)
            ?: throw ApiException(
                ApiExceptions.NO_SUCH_ACTION_IN_ROLE,
                "roleId: $roleId, not found actionId: $actionId"
            )
    }

}