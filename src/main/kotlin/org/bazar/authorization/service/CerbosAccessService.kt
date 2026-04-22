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
        val attributes = authorizationRequest.attributes
            ?.mapValues { AttributeValue.stringValue(it.value) }
        val principal = Principal.newInstance(authorizationRequest.userId.toString(), "user")
            .apply {
                if (attributes != null)
                    withAttributes(attributes)
            }
        val resource = Resource.newInstance(authorizationRequest.resource, authorizationRequest.spaceId.toString())

        val result = cerbosClient.check(principal, resource, authorizationRequest.action)

        return authorizationRequest.creator || result.isAllowed(authorizationRequest.action)
    }
}