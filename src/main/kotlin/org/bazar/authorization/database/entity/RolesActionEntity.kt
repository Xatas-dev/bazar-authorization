package org.bazar.authorization.database.entity

import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass

class RolesActionEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {

    companion object : CompositeEntityClass<RolesActionEntity>(RolesActions)

    var role by RoleEntity referencedOn RolesActions.role

    var action by ActionEntity referencedOn RolesActions.action
    var assignedAttribute by RolesActions.assignedAttribute
    val createdAt by RolesActions.createdAt
    val updatedAt by RolesActions.updatedAt
}