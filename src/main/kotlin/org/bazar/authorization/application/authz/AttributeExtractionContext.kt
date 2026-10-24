package org.bazar.authorization.application.authz

import org.bazar.authorization.domain.spaceuser.SpaceUser

data class AttributeExtractionContext(
    val principalAttributes: Map<String, String>,
    val resourceAttributes: Map<String, String>,
    val authenticatedUser: SpaceUser,
    val resource: String,
    val action: String,
    val resourceId: String?
) {
    fun withPrincipalAttributes(vararg pairs: Pair<String, String>) = copy(
        principalAttributes = principalAttributes + pairs
    )

    fun withPrincipalAttributes(map: Map<String, String>) = copy(
        principalAttributes = principalAttributes + map
    )

    fun withResourceAttributes(vararg pairs: Pair<String, String>) = copy(
        resourceAttributes = resourceAttributes + pairs
    )
}
