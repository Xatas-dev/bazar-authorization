package org.bazar.authorization.adapter.inbound.rest.role

import org.bazar.authorization.application.role.command.CreateRoleCommand
import org.bazar.authorization.application.role.command.RoleActionToGrant
import org.bazar.authorization.application.role.command.UpdateRoleCommand
import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.application.role.EnrichedRole
import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.adapter.inbound.rest.dto.request.CreateRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.request.PutRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.request.SimpleActionDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetActionDto
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
    return GetEnrichedRoleResponse(
        id = this.role.id!!,
        name = this.role.name,
        isVisible = this.role.isVisible,
        createdBy = this.role.createdBy.toString(),
        spaceId = this.role.spaceId,
        actions = actions.map { it.toGetActionDto() }
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
    actionId = this.id
)

private fun Action.toGetActionDto(): GetActionDto {
    return GetActionDto(
        id = this.id,
        code = this.code,
        name = this.name,
        resource = this.resource,
        resourceName = this.resourceName
    )
}
