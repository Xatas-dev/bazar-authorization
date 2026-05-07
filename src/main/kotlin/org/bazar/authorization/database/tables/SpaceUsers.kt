package org.bazar.authorization.database.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.timestamp

object SpaceUsers : LongIdTable("space_user") {
    val spaceId = long("space_id")
    val userId = varchar("user_id", length = 255)
    val role = reference("role_id", Roles)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}