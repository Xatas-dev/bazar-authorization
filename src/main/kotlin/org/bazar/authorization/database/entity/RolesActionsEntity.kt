package org.bazar.authorization.database.entity

import java.time.Instant

data class RolesActionsEntity(
    val roleId: Long,
    val actionId: Int,
    var assignedAttribute: Map<String, String>?,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
) {


}
