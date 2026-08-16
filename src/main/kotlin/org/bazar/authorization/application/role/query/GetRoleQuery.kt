package org.bazar.authorization.application.role.query

import java.util.UUID

data class GetRoleQuery(
    val roleId: Long,
    val spaceId: Long,
    val requesterId: UUID
)
