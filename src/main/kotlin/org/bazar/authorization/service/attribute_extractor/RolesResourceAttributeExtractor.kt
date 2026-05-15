package org.bazar.authorization.service.attribute_extractor

import org.bazar.authorization.service.RoleService

class RolesResourceAttributeExtractor(
    private val roleService: RoleService
) : AttributeExtractor {

    override suspend fun extract(context: AttributeExtractionContext): AttributeExtractionContext {
        if (context.resourceId != null) {
            val targetRole = roleService.getRoleById(context.resourceId.toLong())
            context.resourceAttributes.apply {
                targetRole.createdBy?.let {
                    put("created_by", it.toString())
                }
                put("scope", targetRole.scope.name)
                put("id", targetRole.id.toString())
            }
        }


        return context
    }

    override fun isApplicable(context: AttributeExtractionContext): Boolean {
        return context.resource == "roles"
    }
}