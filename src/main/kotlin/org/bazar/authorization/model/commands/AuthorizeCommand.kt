package org.bazar.authorization.model.commands

import java.util.UUID

data class AuthorizeCommand(
    val spaceId: Long,
    val loggedInUserId: UUID,
    val resource: String,
    val resourceId: String,
    val action: String,
    val principalAttributes: Map<String, String> = emptyMap(),
    val resourceAttributes: Map<String, String> = emptyMap()
)
