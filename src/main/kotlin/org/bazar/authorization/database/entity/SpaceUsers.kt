package org.bazar.authorization.database.entity

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.timestamp

object SpaceUsers : LongIdTable("space_user") {
    val spaceId = long("space_id")
    val userId = varchar("user_id", length = 255)
    val role = reference("role_id", Roles)
    val creator = bool("creator")
    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt")
}