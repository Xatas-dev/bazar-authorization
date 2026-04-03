package org.bazar.authorization.database.entity

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.json.jsonb

object RolesActions : CompositeIdTable("roles_actions") {
    val role = reference("role_id", Roles)
    val action = reference("action_id", Actions)

    val assignedAttribute = jsonb<String>(
        "assigned_attribute",
        serialize = { it },
        deserialize = { it }
    )
    init {
        addIdColumn(role)
        addIdColumn(action)
    }
}