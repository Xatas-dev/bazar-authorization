package org.bazar.authorization.adapter.inbound.rest.action

import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.action.ActionAttribute
import org.bazar.authorization.application.action.ActionsWithAttributes
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetActionAttributeDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetActionDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetActionsResponse

fun ActionsWithAttributes.toResponse(): GetActionsResponse {
    val actionAttributesByActionId = attributes.groupBy { it.actionId }

    return GetActionsResponse(
        actions = actions.map { action ->
            action.toGetActionDto(actionAttributesByActionId[action.id] ?: emptyList())
        }
    )
}

fun ActionAttribute.toGetActionAttributeDto(): GetActionAttributeDto {
    return GetActionAttributeDto(
        id = this.id,
        name = this.name,
        displayName = this.displayName,
        valueType = this.valueType
    )
}

fun Action.toGetActionDto(attributes: List<ActionAttribute>): GetActionDto {
    return GetActionDto(
        id = this.id,
        code = this.code,
        name = this.name,
        resource = this.resource,
        resourceName = this.resourceName,
        attributes = attributes.map { it.toGetActionAttributeDto() }
    )
}
