package org.bazar.authorization.service.attribute_extractor

import org.bazar.authorization.database.entity.SpaceUserEntity
import java.util.UUID

data class AttributeExtractionContext(
    val principalAttributes: MutableMap<String, String> = mutableMapOf(),
    val resourceAttributes: MutableMap<String, String> = mutableMapOf(),
    val authenticatedUser: SpaceUserEntity,
    val resource: String,
    val action: String,
    val resourceId: String
)
