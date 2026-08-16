package org.bazar.authorization.domain.action

import java.time.Instant

data class Action(
    val id: Int,
    val code: String,
    val name: String,
    val resource: String,
    val resourceName: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
