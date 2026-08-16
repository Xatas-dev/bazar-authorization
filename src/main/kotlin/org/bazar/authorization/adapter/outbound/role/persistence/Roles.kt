package org.bazar.authorization.adapter.outbound.role.persistence

import org.bazar.authorization.domain.role.RoleScope
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.timestamp

object Roles : LongIdTable("role") {
    val name = varchar("name", length = 255)
    val spaceId = long("space_id").nullable()
    val scope = enumerationByName(name = "scope", length = 50, klass = RoleScope::class)
    val isVisible = bool("is_visible")
    val createdBy = varchar("created_by", length = 48).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
