package org.bazar.authorization.database.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class ActionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ActionEntity>(Actions)

    var code by Actions.code
    var name by Actions.name
    var resource by Actions.resource
    var createdAt by Actions.createdAt
    var updatedAt by Actions.updatedAt
}