package org.bazar.authorization.utils.extensions.mapper

import org.bazar.authorization.database.entity.ActionAttributeEntity
import org.bazar.authorization.database.entity.ActionEntity
import org.bazar.authorization.model.rest.response.GetActionAttributeDto
import org.bazar.authorization.model.rest.response.GetActionDto

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