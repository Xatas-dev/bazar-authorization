package org.bazar.authorization.utils.extensions

import org.bazar.authorization.database.entity.ActionAttributeEntity
import org.bazar.authorization.database.entity.ActionEntity
import org.bazar.authorization.database.entity.RoleWithActionMappings
import org.bazar.authorization.model.rest.request.CreateRoleRequest
import org.bazar.authorization.model.rest.response.*
import org.bazar.authorization.utils.authorization.enums.KnownAttributes
import org.bazar.authorization.utils.exceptions.ApiException

fun ActionAttributeEntity.toGetActionAttributeDto(): GetActionAttributeDto {
    return GetActionAttributeDto(
        id = this.id,
        name = this.name,
        displayName = this.displayName,
        valueType = this.valueType
    )
}

fun ActionEntity.toGetActionDto(attributes: List<ActionAttributeEntity>): GetActionDto {
    return GetActionDto(
        id = this.id,
        code = this.code,
        name = this.name,
        resource = this.resource,
        attributes = attributes.map { it.toGetActionAttributeDto() }
    )
}


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

fun ApiException.toErrorDto() =
    ErrorResponse(
        code = this.exceptionType.httpStatus.value,
        message = this.exceptionType.displayMessage
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
