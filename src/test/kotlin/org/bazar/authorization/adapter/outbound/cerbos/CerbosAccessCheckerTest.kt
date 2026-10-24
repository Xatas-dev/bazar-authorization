package org.bazar.authorization.adapter.outbound.cerbos

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.builders.AttributeValue
import dev.cerbos.sdk.builders.AttributeValue.listValue
import dev.cerbos.sdk.builders.AttributeValue.stringValue
import dev.cerbos.sdk.builders.Principal
import dev.cerbos.sdk.builders.Resource
import org.bazar.authorization.infrastructure.BaseIntegrationTest
import org.junit.jupiter.api.DisplayName
import org.koin.test.inject
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class CerbosAccessCheckerTest : BaseIntegrationTest() {

    private val cerbosBlockingClient: CerbosBlockingClient by inject<CerbosBlockingClient>();

    @Test
    @DisplayName("User has role with action space_user:ADD should return ALLOW")
    fun test_spaceUserHasSpaceUserAddAction_shouldReturnAllow() = integrationTest {
        val userId = UUID.randomUUID();

        val principal = Principal.newInstance(userId.toString(), "user")
            .withAttribute(
                "allowed_actions", listValue(
                    stringValue("space_user:ADD")
                )
            )
        val resource = Resource.newInstance("space_user")
        val result = cerbosBlockingClient.check(principal, resource, "ADD")
        assertEquals(true, result.isAllowed("ADD"))
    }

    @Test
    @DisplayName("User is creator, should return ALLOW on any action")
    fun test_spaceUserIsCreator_shouldReturnAllow() = integrationTest {
        val userId = UUID.randomUUID();

        val principal = Principal.newInstance(userId.toString(), "user")
            .withAttribute("is_creator", AttributeValue.boolValue(true))
        val resource = Resource.newInstance("space_user")
        val result = cerbosBlockingClient.check(principal, resource, "ADD")
        assertEquals(true, result.isAllowed("ADD"))
    }

    @Test
    @DisplayName("User tries to delete creator of the space, should return DENY")
    fun test_spaceUserCanDeleteUsersAndTargetUserIsCreator_shouldReturnDeny() = integrationTest {
        val userId = UUID.randomUUID();

        val principal = Principal.newInstance(userId.toString(), "user")
            .withAttribute(
                "allowed_actions", listValue(
                    stringValue("space_user:DELETE")
                )
            )
        val resource = Resource.newInstance("space_user")
            .withAttribute("is_creator", AttributeValue.boolValue(true))
        val result = cerbosBlockingClient.check(principal, resource, "DELETE")
        assertEquals(false, result.isAllowed("DELETE"))
    }

    @Test
    @DisplayName("User has all required permission to create a role, should return ALLOW")
    fun test_spaceUserHasRolesCreateAndTriesToCreateRole_shouldReturnAllow() = integrationTest {
        val userId = UUID.randomUUID();

        val principal = Principal.newInstance(userId.toString(), "user")
            .withAttribute(
                "allowed_actions", listValue(
                    stringValue("roles:ASSIGN"),
                    stringValue("roles:MANAGE"),
                    stringValue("space:DELETE"),
                    stringValue("space_user:ADD")
                )
            )
            .withAttribute(
                "actions_to_grant", listValue(
                    stringValue("space:DELETE"),
                    stringValue("space_user:ADD")
                )
            )
        val resource = Resource.newInstance("roles")
        val result = cerbosBlockingClient.check(principal, resource, "MANAGE")
        assertEquals(true, result.isAllowed("MANAGE"))
    }

    @Test
    @DisplayName("User has all required permissions to edit role, should return ALLOW")
    fun test_spaceUserHasRolesEdit_shouldReturnAllow() = integrationTest {
        val userId = UUID.randomUUID();

        val principal = Principal.newInstance(userId.toString(), "user")
            .withAttribute(
                "allowed_actions", listValue(
                    stringValue("roles:ASSIGN"),
                    stringValue("roles:MANAGE"),
                    stringValue("space:DELETE"),
                    stringValue("space_user:ADD")
                )
            )
            .withAttribute(
                "actions_to_grant", listValue(
                    stringValue("roles:ASSIGN"),
                    stringValue("space:DELETE"),
                    stringValue("space_user:ADD")
                )
            )
            .withAttribute(
                "manageable_roles", listValue(
                    stringValue("1")
                )
            )
        val resource = Resource.newInstance("roles")
            .withAttribute("id", stringValue("1"))
            .withAttribute("scope", stringValue("SPACE"))
        val result = cerbosBlockingClient.check(principal, resource, "MANAGE")
        assertEquals(true, result.isAllowed("MANAGE"))
    }

}