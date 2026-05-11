package org.bazar.authorization.service.attribute_extractor

interface AttributeExtractor {

    suspend fun extract(context: AttributeExtractionContext): AttributeExtractionContext

    fun isApplicable(context: AttributeExtractionContext): Boolean
}