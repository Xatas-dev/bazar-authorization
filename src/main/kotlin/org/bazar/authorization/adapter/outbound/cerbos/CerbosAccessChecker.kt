package org.bazar.authorization.adapter.outbound.cerbos

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.builders.AttributeValue
import dev.cerbos.sdk.builders.Principal
import dev.cerbos.sdk.builders.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bazar.authorization.application.shared.port.out.AccessPolicyChecker
import org.bazar.authorization.domain.authz.AuthorizationCheck

class CerbosAccessChecker(
    private val cerbosClient: CerbosBlockingClient
) : AccessPolicyChecker {

    override suspend fun checkAccess(check: AuthorizationCheck): Boolean =
        withContext(Dispatchers.IO) {
            checkAccessBlocking(check)
        }

    private fun checkAccessBlocking(check: AuthorizationCheck): Boolean {
        val principal = Principal.newInstance(check.loggedInUserId.toString(), "user")
            .apply {
                withAttributes(check.principalAttributes.mapValues { toAttributeValue(it.value) })
            }
        val resource = Resource.newInstance(check.resource, check.spaceId.toString())
            .apply {
                withAttributes(check.resourceAttributes.mapValues { toAttributeValue(it.value) })
            }

        val result = cerbosClient.check(principal, resource, check.action)

        return result.isAllowed(check.action)
    }

    private fun toAttributeValue(value: String): AttributeValue {
        return when {
            value.equals("true", ignoreCase = true) ||
                    value.equals("false", ignoreCase = true) ->
                AttributeValue.boolValue(value.toBoolean())

            value.toDoubleOrNull() != null ->
                AttributeValue.doubleValue(value.toDouble())

            value.startsWith("[") && value.endsWith("]") ->
                parseListAttribute(value)

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
