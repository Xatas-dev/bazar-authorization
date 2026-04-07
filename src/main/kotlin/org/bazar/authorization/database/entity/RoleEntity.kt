package org.bazar.authorization.database.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class RoleEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RoleEntity>(table = Roles)

    var name by Roles.name
    var spaceId by Roles.spaceId
    var scope by Roles.scope
    var createdAt by Roles.createdAt
    var updatedAt by Roles.updatedAt
}