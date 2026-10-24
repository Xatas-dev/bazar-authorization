package org.bazar.authorization.adapter.inbound.rest.action

import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetActionAttributeDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetActionDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetActionsResponse
import org.bazar.authorization.adapter.outbound.action.persistence.Actions

fun List<Action>.toResponse(): GetActionsResponse {
    return GetActionsResponse(
        actions = this.map { it.toGetActionDto() }
    )
}

fun Action.toGetActionDto(): GetActionDto {
    return GetActionDto(
        id = this.id,
        code = this.code,
        name = this.name,
        resource = this.resource,
        resourceName = this.resourceName
    )
}
