package org.bazar.authorization.database.entity

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.timestamp

object Actions : IntIdTable("action") {
    val code = varchar("code", length = 64)
    val name = varchar("name", length = 128)
    val resource = varchar("resource", length = 64)
    val createdAt = timestamp("createdAt")
    val updatedAt = timestamp("updatedAt")
}