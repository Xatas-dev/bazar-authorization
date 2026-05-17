package org.bazar.authorization.service.api

import org.bazar.authorization.model.rest.response.GetRoleNameDto
import org.bazar.authorization.model.rest.response.GetRoleNamesResponse
import org.bazar.authorization.service.RoleService
import org.bazar.authorization.service.SpaceUserService
import org.bazar.authorization.utils.logger
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*

class SpaceUserApiService(
    private val spaceUserService: SpaceUserService,
    private val roleService: RoleService
) {
    private val logger = logger()

    suspend fun assignRoleToUser(spaceId: Long, userId: UUID, roleId: Long) = suspendTransaction {
        spaceUserService.assignRoleToUser(userId, spaceId, roleId)
    }

    suspend fun getRoleNames(spaceId: Long, userIds: List<UUID>) = suspendTransaction {
        val users = spaceUserService.getAllUsers(spaceId, userIds)

        val rolesMap = roleService.getAllRolesByIds(users.map { it.roleId })
            .associateBy { it.id!! }

        val roleDtos = users.mapNotNull { user ->
            val role = rolesMap[user.roleId]

            if (role != null) {
                GetRoleNameDto(
                    id = role.id!!,
                    name = role.name,
                    userId = user.userId.toString(),
                    isVisible = role.isVisible,
                    isCreator = user.isCreator
                )
            } else {
                logger.warn("roleId=${user.roleId} not found for user ${user.userId}")
                null
            }
        }

        GetRoleNamesResponse(roles = roleDtos)
    }

}
