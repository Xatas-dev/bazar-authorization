package org.bazar.authorization.database.entity

import org.bazar.authorization.database.entity.enums.Role
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

object UserSpaceRoleTable : Table("user_space_role") {
    val spaceId = long("space_id")
    val userId = javaUUID("user_id")
    val role = enumerationByName("role", 50, Role::class)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(spaceId, userId)
}