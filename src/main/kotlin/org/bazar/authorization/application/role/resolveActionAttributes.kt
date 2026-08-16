package org.bazar.authorization.application.role

import org.bazar.authorization.application.role.command.RoleActionToGrant
import org.bazar.authorization.domain.action.ActionAttribute
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException

fun resolveActionAttributes(
    actions: List<RoleActionToGrant>,
    attributeEntities: List<ActionAttribute>
): Map<Int, Map<String, String>> {
    val attrIdToNameMap = attributeEntities.associate { it.id to it.name }

    return actions.associate { actionReq ->
        val attrNameToValue = actionReq.attributes.associate { attr ->
            val name = attrIdToNameMap[attr.attributeId]
                ?: throw DomainException(DomainErrors.NO_SUCH_ATTRIBUTE, "id = ${attr.attributeId}")
            name to attr.value
        }
        actionReq.actionId to attrNameToValue
    }
}
