package org.bazar.authorization.domain.action

import java.time.Instant

data class ActionAttribute(
    val id: Int,
    val actionId: Int,
    val name: String,
    val displayName: String,
    val valueType: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
