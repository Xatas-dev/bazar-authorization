package org.bazar.authorization.database.entity

import java.time.Instant

data class ActionAttributeEntity(
    val id: Int,
    val actionId: Int,
    val name: String,
    val displayName: String,
    val valueType: String,
    val createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
)
