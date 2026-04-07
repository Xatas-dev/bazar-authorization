package org.bazar.authorization.database.entity

import org.bazar.authorization.database.entity.enums.RoleScope
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.timestamp

object Roles : LongIdTable("role") {
    val name = varchar("name", length = 255).nullable()
    val spaceId = long("space_id").nullable()
    val scope = enumerationByName(name = "scope", length = 50, klass = RoleScope::class)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}