package org.bazar.authorization.adapter.inbound.rest.role

import org.bazar.authorization.application.role.command.CreateRoleCommand
import org.bazar.authorization.application.role.command.RoleActionToGrant
import org.bazar.authorization.application.role.command.RoleAttributeToGrant
import org.bazar.authorization.application.role.command.UpdateRoleCommand
import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.action.ActionAttribute
import org.bazar.authorization.application.role.EnrichedRole
import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.adapter.inbound.rest.dto.request.CreateRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.request.PutRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.request.SimpleActionDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetActionWithAssignedAttributesDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetAssignedActionAttributeDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetEnrichedRoleResponse
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetRoleDto
import java.util.UUID

fun CreateRoleRequest.toCommand(loggedUserId: UUID) = CreateRoleCommand(
    name = this.name,
    spaceId = this.spaceId,
    isVisible = this.isVisible,
    createdBy = loggedUserId,
    actions = this.actions.map { it.toRoleActionToGrant() }
)

fun PutRoleRequest.toCommand(roleId: Long, spaceId: Long, requesterId: UUID) = UpdateRoleCommand(
    roleId = roleId,
    spaceId = spaceId,
    requesterId = requesterId,
    name = this.name,
    isVisible = this.isVisible,
    actions = this.actions.map { it.toRoleActionToGrant() }
)

fun EnrichedRole.toResponse(): GetEnrichedRoleResponse {
    val actionIdToAttributes = actionAttributes.groupBy { it.actionId }
    val actionIdToAttributeValues = actionMappings.associate { it.actionId to it.assignedAttributes }

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

fun Role.toGetRoleDto() = GetRoleDto(
    id = id!!,
    name = name,
    spaceId = spaceId!!,
    scope = scope.name,
    isVisible = isVisible,
    createdBy = createdBy?.toString()
)

private fun SimpleActionDto.toRoleActionToGrant() = RoleActionToGrant(
    actionId = this.id,
    attributes = this.attributes.map { RoleAttributeToGrant(it.id, it.value) }
)

private fun Action.toGetActionWithAssignedAttributesDto(
    attributes: List<ActionAttribute>?,
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
        resourceName = this.resourceName,
        attributes = assignedAttributes
    )
}

private fun ActionAttribute.toGetAssignedActionAttributeDto(value: String) =
    GetAssignedActionAttributeDto(
        id = this.id,
        name = this.name,
        displayName = this.displayName,
        valueType = this.valueType,
        value = value
    )
