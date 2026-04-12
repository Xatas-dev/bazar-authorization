package org.bazar.authorization.controller

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.bazar.authorization.infrastructure.BaseWebTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class SpaceUserControllerTest : BaseWebTest() {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    @DisplayName("Two users in space, requester has role = CREATOR, should read target role successfully")
    fun requesterWithCreatorRole_shouldReadTargetRole() = webTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000002")

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = 1, creator = false)
        initDataHelper.createSpaceUser(spaceId, targetUserId, roleId = 2, creator = false)

        val response = client.get("/api/v1/space-users/roles?spaceId=$spaceId&userId=$targetUserId") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val expected = loadExpectedJson("/expected/space-user/ok-role2.json")
        assertJsonEquals(expected, response.bodyAsText())
    }

    @Test
    @DisplayName("Two users in space, requester has roleId=2, should get forbidden")
    fun requesterWithLimitedRole_shouldGetForbidden() = webTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000003")

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = 2, creator = false)
        initDataHelper.createSpaceUser(spaceId, targetUserId, roleId = 2, creator = false)

        val response = client.get("/api/v1/space-users/roles?spaceId=$spaceId&userId=$targetUserId") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)

        val expected = loadExpectedJson("/expected/space-user/forbidden.json")
        assertJsonEquals(expected, response.bodyAsText())
    }

    @Test
    @DisplayName("Two users in space, requester has roleId=1 and target has custom role, should return ok")
    fun requesterWithCreatorRoleAndTargetWithCustomRole_shouldReturnOk() = webTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000004")
        val customRoleId = initDataHelper.createRole()

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = 1, creator = false)
        initDataHelper.createSpaceUser(spaceId, targetUserId, roleId = customRoleId, creator = false)

        val response = client.get("/api/v1/space-users/roles?spaceId=$spaceId&userId=$targetUserId") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val expected = loadExpectedJson("/expected/space-user/ok-custom-role.json")
            .replace("{{roleId}}", customRoleId.toString())
        assertJsonEquals(expected, response.bodyAsText())
    }

    @Test
    @DisplayName("Requester with grantable_actions [1,2] assigns actions 1 and 2, should return ok")
    fun createRole_withAllowedActions_shouldReturnOk() = webTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000005")

        val requesterRoleId = initDataHelper.createRole()
        initDataHelper.createRolesActions(
            roleId = requesterRoleId,
            actionId = 8,
            assignedAttributes = mapOf("grantable_actions" to "[1,2]")
        )

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = requesterRoleId, creator = false)
        initDataHelper.createSpaceUser(spaceId, targetUserId, roleId = 2, creator = false)

        val response = client.post("/api/v1/space-users/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(createRoleRequestJson(targetUserId, spaceId, listOf(1, 2)))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val expected = loadExpectedJson("/expected/space-user/post-role-ok-actions-1-2.json")
            .replace("{{roleId}}", extractRoleId(response.bodyAsText()))
        assertJsonEquals(expected, response.bodyAsText())
    }

    @Test
    @DisplayName("Requester with grantable_actions [1,2] assigns actions 1,2,3, should get forbidden")
    fun createRole_withForbiddenAction_shouldReturnForbidden() = webTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000006")

        val requesterRoleId = initDataHelper.createRole()
        initDataHelper.createRolesActions(
            roleId = requesterRoleId,
            actionId = 8,
            assignedAttributes = mapOf("grantable_actions" to "[1,2]")
        )

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = requesterRoleId, creator = false)
        initDataHelper.createSpaceUser(spaceId, targetUserId, roleId = 2, creator = false)

        val response = client.post("/api/v1/space-users/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(createRoleRequestJson(targetUserId, spaceId, listOf(1, 2, 3)))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)

        val expected = loadExpectedJson("/expected/space-user/post-role-forbidden-actions-1-2-3.json")
        assertJsonEquals(expected, response.bodyAsText())
    }

    private fun createRoleRequestJson(userId: UUID, spaceId: Long, actionIds: List<Int>): String {
        val actionsJson = actionIds.joinToString(",") { "{\"id\":$it,\"attributes\":[]}" }
        return "{\"userId\":\"$userId\",\"spaceId\":$spaceId,\"actions\":[${actionsJson}]}"
    }

    private fun extractRoleId(responseBody: String): String {
        val jsonObject = json.parseToJsonElement(responseBody) as? JsonObject
            ?: throw IllegalStateException("Response body is not a JSON object")

        return jsonObject["id"]?.toString()
            ?: throw IllegalStateException("Response has no id field")
    }

    private fun assertJsonEquals(expectedJson: String, actualJson: String) {
        assertEquals(json.parseToJsonElement(expectedJson), json.parseToJsonElement(actualJson))
    }

    private fun loadExpectedJson(path: String): String {
        return this::class.java.getResourceAsStream(path)
            ?.bufferedReader()
            ?.readText()
            ?: throw IllegalStateException("Expected response file not found: $path")
    }

    private fun randomSpaceId(): Long = kotlin.math.abs(UUID.randomUUID().mostSignificantBits)
}
