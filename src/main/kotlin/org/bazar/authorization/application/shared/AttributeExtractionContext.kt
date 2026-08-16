package org.bazar.authorization.application.shared

import org.bazar.authorization.domain.spaceuser.SpaceUser

data class AttributeExtractionContext(
    val principalAttributes: MutableMap<String, String> = mutableMapOf(),
    val resourceAttributes: MutableMap<String, String> = mutableMapOf(),
    val authenticatedUser: SpaceUser,
    val resource: String,
    val action: String,
    val resourceId: String?
)
