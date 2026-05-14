package org.bazar.authorization.service.api

import org.bazar.authorization.model.rest.request.CreateRoleRequest
import org.bazar.authorization.model.rest.request.PutRoleRequest
import org.bazar.authorization.model.rest.response.GetRoleNameDto
import org.bazar.authorization.model.rest.response.GetRoleNamesResponse
import org.bazar.authorization.model.rest.response.GetRolesResponse
import org.bazar.authorization.service.ActionAttributeService
import org.bazar.authorization.service.ActionService
import org.bazar.authorization.service.RoleService
import org.bazar.authorization.service.SpaceUserService
import org.bazar.authorization.utils.extensions.mapper.toCreateRoleCommand
import org.bazar.authorization.utils.extensions.mapper.toGetSpaceUsersRoleResponse
import org.bazar.authorization.utils.extensions.mapper.toUpdateRoleCommand
import org.bazar.authorization.utils.logger
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*

class RolesApiService(
    private val spaceUserService: SpaceUserService,
    private val roleService: RoleService,
    private val actionService: ActionService,
    private val actionAttributeService: ActionAttributeService
) {

    private val logger = logger()

    /*
        Get detailed role with actions and assigned attributes
        Logic:
            1. Get space user and his role
            2. Get role info from role table
            3. Get assigned actions and attributes from roles_actions
            4. Get actions`q1`1
     */
    suspend fun getRoleWithActionsAndAttributes(roleId: Long): GetRolesResponse =
        suspendTransaction {
            val roleWithMappings = roleService.getRoleWithActionMappings(roleId)
            val actions = actionService.findAllByIds(roleWithMappings.actionMappings.map { it.actionId })
            val attributes = actionAttributeService.getAllAttributes(actions.map { it.id })

            roleWithMappings.toGetSpaceUsersRoleResponse(actions, attributes)
        }

    /*
        Create new USER scope role and delete previous role if it was USER scope
     */
    suspend fun createRole(request: CreateRoleRequest, loggedUserId: UUID) = suspendTransaction {
        val attributeEntities = actionAttributeService.getAllAttributesByIds(
            request.actions.flatMap { it.attributes.map { attr -> attr.id } }
        )

        val createRoleCommand = request.toCreateRoleCommand(attributeEntities, loggedUserId)

        val newRole = roleService.createSpaceScopedRole(createRoleCommand)

        val actions = actionService.findAllByIds(request.actions.map { it.id })

        newRole.toGetSpaceUsersRoleResponse(actions, attributeEntities)
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
                    isVisible = role.isVisible
                )
            } else {
                logger.warn("roleId=${user.roleId} not found for user ${user.userId}")
                null
            }
        }

        GetRoleNamesResponse(roles = roleDtos)
    }

    suspend fun updateRole(request: PutRoleRequest, roleId: Long) = suspendTransaction {
        val attributeEntities = actionAttributeService.getAllAttributesByIds(
            request.actions.flatMap { it.attributes.map { attr -> attr.id } }
        )

        val updateRoleCommand = request.toUpdateRoleCommand(roleId, attributeEntities)
        val updatedRole = roleService.updateRole(updateRoleCommand)
        val actions = actionService.findAllByIds(request.actions.map { it.id })

        updatedRole.toGetSpaceUsersRoleResponse(actions, attributeEntities)
    }

}