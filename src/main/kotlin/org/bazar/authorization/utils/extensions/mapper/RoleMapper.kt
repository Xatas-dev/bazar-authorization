package org.bazar.authorization.utils.extensions.mapper

import org.bazar.authorization.database.entity.ActionAttributeEntity
import org.bazar.authorization.database.entity.ActionEntity
import org.bazar.authorization.database.entity.RoleEntity
import org.bazar.authorization.database.entity.RoleWithActionMappings
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.model.commands.CreateRoleCommand
import org.bazar.authorization.model.rest.request.CreateRoleRequest
import org.bazar.authorization.model.rest.response.GetActionWithAssignedAttributesDto
import org.bazar.authorization.model.rest.response.GetAssignedActionAttributeDto
import org.bazar.authorization.model.rest.response.GetSpaceUsersRoleResponse
import org.bazar.authorization.utils.authorization.enums.KnownAttributes
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import java.util.UUID

fun CreateRoleRequest.toCreateRoleCommand(
    attributeEntities: List<ActionAttributeEntity>,
    loggedUserId: UUID
): CreateRoleCommand {
    val attrIdToNameMap = attributeEntities.associate { it.id to it.name }

    val actionsWithAttributes = this.actions.associate { actionReq ->
        val attrNameToValue = actionReq.attributes.associate { attr ->
            attrIdToNameMap[attr.id]?.let { name -> name to attr.value }
                ?: throw ApiException(ApiExceptions.NO_SUCH_ATTRIBUTE, "id = ${attr.id}")
        }

        actionReq.id to attrNameToValue
    }

    return CreateRoleCommand(
        name = this.name,
        spaceId = this.spaceId,
        scope = RoleScope.SPACE,
        isVisible = this.isVisible,
        createdBy = loggedUserId,
        actionsWithAttributes
    )
}

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
): GetSpaceUsersRoleResponse {
    val actionIdToAttributes = actionAttributes.groupBy { it.actionId }
    val actionIdToAttributeValues = this.actionMappings.associate { it.actionId to it.assignedAttributes }

    return GetSpaceUsersRoleResponse(
        id = this.role.id!!,
        name = this.role.name,
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