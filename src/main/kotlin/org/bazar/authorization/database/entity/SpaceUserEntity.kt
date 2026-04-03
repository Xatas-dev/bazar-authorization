package org.bazar.authorization.database.entity

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class SpaceUserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SpaceUserEntity>(SpaceUsers)

    var spaceId by SpaceUsers.spaceId
    var userId by SpaceUsers.userId
    val role by RoleEntity referencedOn SpaceUsers.role
    var creator by SpaceUsers.creator
    var createdAt by SpaceUsers.createdAt
    var updatedAt by SpaceUsers.updatedAt
}