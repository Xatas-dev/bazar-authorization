package org.bazar.authorization.application.authz.extractors

import org.bazar.authorization.application.shared.AttributeExtractionContext

interface AttributeExtractor {
    fun extract(context: AttributeExtractionContext): AttributeExtractionContext

    fun isApplicable(context: AttributeExtractionContext): Boolean
}
