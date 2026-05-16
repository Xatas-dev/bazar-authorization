package org.bazar.authorization.utils.extensions.mapper

import org.bazar.authorization.database.entity.ActionAttributeEntity
import org.bazar.authorization.database.entity.ActionEntity
import org.bazar.authorization.database.entity.RoleEntity
import org.bazar.authorization.database.entity.RoleWithActionMappings
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.model.commands.CreateRoleCommand
import org.bazar.authorization.model.commands.UpdateRoleCommand
import org.bazar.authorization.model.rest.request.CreateRoleRequest
import org.bazar.authorization.model.rest.request.PutRoleRequest
import org.bazar.authorization.model.rest.request.SimpleActionDto
import org.bazar.authorization.model.rest.response.GetActionWithAssignedAttributesDto
import org.bazar.authorization.model.rest.response.GetAssignedActionAttributeDto
import org.bazar.authorization.model.rest.response.GetEnrichedRoleResponse
import org.bazar.authorization.model.rest.response.GetRoleDto
import org.bazar.authorization.utils.authorization.enums.KnownAttributes
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import java.time.Instant
import java.util.*

fun CreateRoleRequest.toCreateRoleCommand(
    attributeEntities: List<ActionAttributeEntity>,
    loggedUserId: UUID
) = CreateRoleCommand(
    name = this.name,
    spaceId = this.spaceId,
    scope = RoleScope.SPACE,
    isVisible = this.isVisible,
    createdBy = loggedUserId,
    actionIdToAttributes = this.actions.toActionsWithAttributesMap(attributeEntities)
)

fun PutRoleRequest.toUpdateRoleCommand(
    id: Long,
    attributeEntities: List<ActionAttributeEntity>
) = UpdateRoleCommand(
    roleId = id,
    name = this.name,
    isVisible = this.isVisible,
    actionIdToAttributes = this.actions.toActionsWithAttributesMap(attributeEntities)
)

fun UpdateRoleCommand.toRoleEntity(existingRole: RoleEntity) =
    RoleEntity(
        scope = existingRole.scope,
        name = name,
        isVisible = isVisible,
        createdBy = existingRole.createdBy,
        spaceId = existingRole.spaceId,
        id = existingRole.id,
        createdAt = existingRole.createdAt,
        updatedAt = Instant.now()
    )

fun CreateRoleCommand.toRoleEntity() =
    RoleEntity(
        scope,
        name,
        isVisible,
        createdBy,
        spaceId
    )

fun RoleWithActionMappings.toGetSpaceUsersRoleResponse(
    actions: List<ActionEntity>,
    actionAttributes: List<ActionAttributeEntity>
): GetEnrichedRoleResponse {
    val actionIdToAttributes = actionAttributes.groupBy { it.actionId }
    val actionIdToAttributeValues = this.actionMappings.associate { it.actionId to it.assignedAttributes }

    return GetEnrichedRoleResponse(
        id = this.role.id!!,
        name = this.role.name,
        isVisible = this.role.isVisible,
        createdBy = this.role.createdBy.toString(),
        spaceId = this.role.spaceId,
        actions = actions.map {
            it.toGetActionWithAssignedAttributesDto(
                actionIdToAttributes[it.id],
                actionIdToAttributeValues[it.id]
            )
        }
    )
}

fun CreateRoleRequest.extractActionsToGrant() =
    mapOf(KnownAttributes.ACTIONS_TO_GRANT.name.lowercase() to this.actions.map { it.id }.toString())

fun PutRoleRequest.extractActionsToGrant() =
    mapOf(KnownAttributes.ACTIONS_TO_GRANT.name.lowercase() to this.actions.map { it.id }.toString())

fun RoleEntity.toGetRoleDto() =
    GetRoleDto(
        id!!, name, spaceId!!, scope.name, isVisible, createdBy?.toString()
    )

private fun ActionEntity.toGetActionWithAssignedAttributesDto(
    attributes: List<ActionAttributeEntity>?,
    attributeValuesByName: Map<String, String>?
): GetActionWithAssignedAttributesDto {

    val assignedAttributes = if (attributeValuesByName == null) {
        emptyList()
    } else {
        attributes?.filter {
            attributeValuesByName.contains(it.name)
        }?.map {
            it.toGetAssignedActionAttributeDto(attributeValuesByName[it.name] ?: "")
        } ?: emptyList()
    }

    return GetActionWithAssignedAttributesDto(
        id = this.id,
        code = this.code,
        name = this.name,
        resource = this.resource,
        attributes = assignedAttributes
    )
}

private fun ActionAttributeEntity.toGetAssignedActionAttributeDto(value: String) =
    GetAssignedActionAttributeDto(
        id = this.id,
        name = this.name,
        displayName = this.displayName,
        valueType = this.valueType,
        value = value
    )

private fun Iterable<SimpleActionDto>.toActionsWithAttributesMap(
    attributeEntities: List<ActionAttributeEntity>
): Map<Int, Map<String, String>> {
    val attrIdToNameMap = attributeEntities.associate { it.id to it.name }

    return this.associate { actionReq ->
        val attrNameToValue = actionReq.attributes.associate { attr ->
            val name = attrIdToNameMap[attr.id]
                ?: throw ApiException(ApiExceptions.NO_SUCH_ATTRIBUTE, "id = ${attr.id}")
            name to attr.value
        }
        actionReq.id to attrNameToValue
    }
}