package org.bazar.authorization.application.authz.extractors.impl

import org.bazar.authorization.application.authz.extractors.AttributeExtractor
import org.bazar.authorization.application.shared.AttributeExtractionContext
import org.bazar.authorization.domain.authz.PrincipalAttributes

class UserPrincipalAttributesExtractor: AttributeExtractor {
    override fun extract(context: AttributeExtractionContext): AttributeExtractionContext {
        val user = context.authenticatedUser

        return context.withPrincipalAttributes(
            PrincipalAttributes.USER_ID to user.userId.toString(),
            PrincipalAttributes.IS_CREATOR to user.isCreator.toString()
        )
    }

    override fun isApplicable(context: AttributeExtractionContext): Boolean = true
}