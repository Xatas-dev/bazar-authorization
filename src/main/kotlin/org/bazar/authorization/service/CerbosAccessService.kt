package org.bazar.authorization.service

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.builders.AttributeValue
import dev.cerbos.sdk.builders.Principal
import dev.cerbos.sdk.builders.Resource
import org.bazar.authorization.model.authz.AuthorizationRequest

class CerbosAccessService(
    private val cerbosClient: CerbosBlockingClient
) {

    fun checkAccess(authorizationRequest: AuthorizationRequest): Boolean {

        val principalAttributes = authorizationRequest.principalAttributes
            .mapValues { toAttributeValue(it.value) }
        val resourceAttributes = authorizationRequest.resourceAttributes
            .mapValues { toAttributeValue(it.value) }

        val principal = Principal.newInstance(authorizationRequest.userId.toString(), "user")
            .apply {
                withAttributes(principalAttributes)
            }
        val resource = Resource.newInstance(authorizationRequest.resource, authorizationRequest.spaceId.toString())
            .apply { withAttributes(resourceAttributes) }

        val result = cerbosClient.check(principal, resource, authorizationRequest.action)

        return result.isAllowed(authorizationRequest.action)
    }

    private fun toAttributeValue(value: String): AttributeValue {
        return when {
            // Boolean
            value.equals("true", ignoreCase = true) ||
                    value.equals("false", ignoreCase = true) ->
                AttributeValue.boolValue(value.toBoolean())

            // Double
            value.toDoubleOrNull() != null ->
                AttributeValue.doubleValue(value.toDouble())

            // Array
            value.startsWith("[") && value.endsWith("]") ->
                parseListAttribute(value)

            //String
            else ->
                AttributeValue.stringValue(value)
        }
    }

    private fun parseListAttribute(value: String): AttributeValue {
        val items = value
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }

        val asDoubles = items.mapNotNull { it.toDoubleOrNull() }
        if (asDoubles.size == items.size) {
            return AttributeValue.listValue(asDoubles.map { AttributeValue.doubleValue(it) })
        }

        return AttributeValue.listValue(items.map { AttributeValue.stringValue(it) })
    }
}