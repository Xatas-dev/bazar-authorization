package org.bazar.authorization.service.api

import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.model.rest.request.CreateRoleRequest
import org.bazar.authorization.model.rest.response.GetSpaceUsersRoleResponse
import org.bazar.authorization.service.ActionAttributeService
import org.bazar.authorization.service.ActionService
import org.bazar.authorization.service.RoleService
import org.bazar.authorization.service.SpaceUserService
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.toGetSpaceUsersRoleResponse
import org.bazar.authorization.utils.extensions.toUuid
import org.bazar.authorization.utils.logger
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.UUID

class SpaceUserApiService(
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
    suspend fun getRoleWithActionsAndAttributes(spaceId: Long, userId: UUID): GetSpaceUsersRoleResponse =
        suspendTransaction {
            val spaceUser = spaceUserService.getSpaceUser(spaceId, userId)
            val roleWithMappings = roleService.getRoleWithActionMappings(spaceUser.roleId)
            val actions = actionService.findAllByIds(roleWithMappings.actionMappings.map { it.actionId })
            val attributes = actionAttributeService.getAllAttributes(actions.map { it.id })

            roleWithMappings.toGetSpaceUsersRoleResponse(actions, attributes)
        }

    /*
        Create new USER scope role and delete previous role if it was USER scope
     */
    suspend fun createRoleAndDeletePrev(request: CreateRoleRequest) = suspendTransaction {
        val roleIdToDelete = spaceUserService.getSpaceUser(request.spaceId, request.userId.toUuid()).roleId

        val attributeEntities = actionAttributeService.getAllAttributesByIds(
            request.actions.flatMap { it.attributes.map { attr -> attr.id } }
        )

        val attrIdToNameMap = attributeEntities.associate { it.id to it.name }

        val actionsWithAttributes = request.actions.associate { actionReq ->
            val attrNameToValue = actionReq.attributes.mapNotNull { attr ->
                attrIdToNameMap[attr.id]?.let { name -> name to attr.value }
                    ?: throw ApiException(ApiExceptions.NO_SUCH_ATTRIBUTE, "id = ${attr.id}")
            }.toMap()

            actionReq.id to attrNameToValue
        }

        val newRole = roleService.createUserScopedRole(actionsWithAttributes)
        spaceUserService.assignRoleToUser(request.userId.toUuid(), request.spaceId, newRole.role.id!!)
        roleService.deleteAllByRoleIdsAndScope(listOf(roleIdToDelete), RoleScope.USER)

        val actions = actionService.findAllByIds(actionsWithAttributes.keys.toList())

        newRole.toGetSpaceUsersRoleResponse(actions, attributeEntities)
    }
}
