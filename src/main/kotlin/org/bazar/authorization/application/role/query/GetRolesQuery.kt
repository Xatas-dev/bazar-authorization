package org.bazar.authorization.application.role.query

import java.util.UUID

data class GetRolesQuery(
    val spaceId: Long,
    val requesterId: UUID
)
