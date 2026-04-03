package org.bazar.authorization.database.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class ActionAttributeEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ActionAttributeEntity>(ActionAttributes)

    val action by ActionEntity referencedOn ActionAttributes.action
    var name by ActionAttributes.name
    var displayName by ActionAttributes.displayName
    var valueType by ActionAttributes.valueType
    var createdAt by ActionAttributes.createdAt
    var updatedAt by ActionAttributes.updatedAt
}