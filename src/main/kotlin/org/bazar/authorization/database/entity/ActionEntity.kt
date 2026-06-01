package org.bazar.authorization.database.entity

import java.time.Instant

data class ActionEntity(
    val id: Int,
    val code: String,
    val name: String,
    val resource: String,
    val resourceName: String,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)
