package org.bazar.authorization.service

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.builders.AttributeValue
import dev.cerbos.sdk.builders.AttributeValue.listValue
import dev.cerbos.sdk.builders.AttributeValue.stringValue
import dev.cerbos.sdk.builders.Principal
import dev.cerbos.sdk.builders.Resource
import org.bazar.authorization.infrastructure.BaseIntegrationTest
import org.koin.test.inject
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class CerbosAccessServiceTest : BaseIntegrationTest() {

    private val cerbosBlockingClient: CerbosBlockingClient by inject<CerbosBlockingClient>();

    @Test
    fun test_spaceUserActionWriteWithAttributeGrantableActionsShouldWork() = integrationTest {
        val userId = UUID.randomUUID();

        val principal = Principal.newInstance(userId.toString(), "user")
            .withAttribute(
                "actions_to_grant", listValue(
                    stringValue("1")
                )
            )
            .withAttribute(
                "grantable_actions", listValue(
                    stringValue("1"),
                    stringValue("2"),
                    stringValue("3"),
                )
            )
        val resource = Resource.newInstance("space_user_actions")
        val result = cerbosBlockingClient.check(principal, resource, "WRITE")
        assertEquals(true, result.isAllowed("WRITE"))
    }

}