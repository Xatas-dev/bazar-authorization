package org.bazar.authorization.application.shared.impl

import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.shared.AttributeExtractor
import org.bazar.authorization.application.shared.AttributeExtractionContext
import org.bazar.authorization.domain.authz.Permission
import org.bazar.authorization.domain.authz.ResourceAttributes
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException

class RolesResourceAttributeExtractor(
    private val roleRepositoryPort: RoleRepositoryPort
) : AttributeExtractor {

    override fun extract(context: AttributeExtractionContext): AttributeExtractionContext {
        if (context.resourceId != null) {
            val targetRole = roleRepositoryPort.findById(context.resourceId.toLong())
                ?: throw DomainException(DomainErrors.NO_SUCH_ROLE, "roleId: ${context.resourceId}")
            context.resourceAttributes.apply {
                targetRole.createdBy?.let {
                    put(ResourceAttributes.CREATED_BY, it.toString())
                }
                put(ResourceAttributes.SCOPE, targetRole.scope.name)
                put(ResourceAttributes.ID, targetRole.id.toString())
            }
        }

        return context
    }

    override fun isApplicable(context: AttributeExtractionContext): Boolean =
        context.resource == Permission.ROLES_READ.resource
}
