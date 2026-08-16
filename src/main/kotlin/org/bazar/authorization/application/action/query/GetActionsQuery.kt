package org.bazar.authorization.application.action.query

import java.util.UUID

data class GetActionsQuery(
    val spaceId: Long,
    val requesterId: UUID
)
