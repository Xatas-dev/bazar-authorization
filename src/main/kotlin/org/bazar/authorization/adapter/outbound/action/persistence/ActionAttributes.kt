package org.bazar.authorization.adapter.outbound.action.persistence

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.timestamp

object ActionAttributes : IntIdTable("action_attribute") {
    val action = reference("action_id", Actions)
    val name = varchar("name", length = 255)
    val displayName = varchar("display_name", length = 255)
    val valueType = varchar("value_type", length = 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
