package org.bazar.authorization.adapter.inbound.rest.role

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.assertj.core.api.Assertions.assertThat
import org.bazar.authorization.infrastructure.BaseWebTest
import org.bazar.authorization.adapter.inbound.rest.dto.request.CreateRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.request.PutRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.request.SimpleActionDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetRoleNamesResponse
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetEnrichedRoleResponse
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetRolesResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class RolesControllerTest : BaseWebTest() {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    companion object {
        private const val DEFAULT_USER_ROLE_ID = 1L
    }

    @Test
    @DisplayName("GET /api/v1/roles, requester is a creator, should return all roles in space")
    fun requesterWithCreatorRole_shouldGetAllSpaceRoles() = webTest {
        val spaceId = randomSpaceId()

        initDataHelper.createRole(spaceId)
        initDataHelper.createRole(spaceId)

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = DEFAULT_USER_ROLE_ID, isCreator = true)

        val response = client.get("/api/v1/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            parameter("spaceId", spaceId)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseDto = response.body<GetRolesResponse>()

        assertThat(responseDto.roles).hasSize(2)
    }

    @Test
    @DisplayName("Two users in space, requester is creator, should read target role successfully")
    fun requesterWithCreatorRole_shouldReadTargetRole() = webTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000002")

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = DEFAULT_USER_ROLE_ID, isCreator = true)
        initDataHelper.createSpaceUser(spaceId, targetUserId, roleId = DEFAULT_USER_ROLE_ID, isCreator = false)

        val response = client.get("/api/v1/roles/$DEFAULT_USER_ROLE_ID") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            parameter("spaceId", spaceId)
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val expected = loadExpectedJson("/expected/space-user/ok-role2.json")
        assertJsonEquals(expected, response.bodyAsText())
    }

    @Test
    @DisplayName("Custom role exist in space, requester has role = Дефолтыч, should get forbidden")
    fun requesterWithLimitedRole_shouldGetForbidden() = webTest {
        val spaceId = randomSpaceId()

        val createdRoleId = initDataHelper.createRole(spaceId)
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = DEFAULT_USER_ROLE_ID, isCreator = false)

        val response = client.get("/api/v1/roles/$createdRoleId") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            parameter("spaceId", spaceId)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)

        val expected = loadExpectedJson("/expected/space-user/forbidden.json")
        assertJsonEquals(expected, response.bodyAsText())
    }

    @Test
    @DisplayName("Two users in space, requester has role = Дефолтыч and target has custom role, should return ok")
    fun requesterWithCreatorRoleAndTargetWithCustomRole_shouldReturnOk() = webTest {
        val spaceId = randomSpaceId()
        val targetUserId = UUID.fromString("00000000-0000-0000-0000-000000000004")
        val customRoleId = initDataHelper.createRole(spaceId, authenticatedUserId)

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = DEFAULT_USER_ROLE_ID, isCreator = true)
        initDataHelper.createSpaceUser(spaceId, targetUserId, roleId = customRoleId, isCreator = false)

        val response = client.get("/api/v1/roles/$customRoleId") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            parameter("spaceId", spaceId)
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val expected = loadExpectedJson("/expected/space-user/ok-custom-role.json")
            .replace("{{roleId}}", customRoleId.toString())
            .replace("{{spaceId}}", spaceId.toString())
            .replace("{{created_by}}", authenticatedUserId.toString())
        assertJsonEquals(expected, response.bodyAsText())
    }

    @Test
    @DisplayName("Requester with grantable_actions [1,2] creates role with actions 1 and 2, should return ok")
    fun createRole_withAllowedActions_shouldReturnOk() = webTest {
        val spaceId = randomSpaceId()

        val requesterRoleId = initDataHelper.createRole(spaceId)
        initDataHelper.createRolesActions(
            roleId = requesterRoleId,
            actionId = 11,
            assignedAttributes = mapOf("grantable_actions" to "[1,2]")
        )

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = requesterRoleId, isCreator = false)

        val request = CreateRoleRequest(
            spaceId,
            "TestRole",
            true,
            listOf(
                SimpleActionDto(1),
                SimpleActionDto(2),
            )
        )

        val response = client.post("/api/v1/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val expected = loadExpectedJson("/expected/space-user/post-role-ok-actions-1-2.json")
            .replace("{{roleId}}", extractRoleId(response.bodyAsText()))
            .replace("{{spaceId}}", spaceId.toString())
        assertJsonEquals(expected, response.bodyAsText())
    }


    @Test
    @DisplayName("Requester with grantable_actions [1,2] creates role with actions 1,2,3, should response forbidden")
    fun createRole_withForbiddenAction_shouldReturnForbidden() = webTest {
        val spaceId = randomSpaceId()

        val requesterRoleId = initDataHelper.createRole(spaceId)
        initDataHelper.createRolesActions(
            roleId = requesterRoleId,
            actionId = 11,
            assignedAttributes = mapOf("grantable_actions" to "[1,2]")
        )

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = requesterRoleId, isCreator = false)

        val request = CreateRoleRequest(
            spaceId,
            "TestRole",
            true,
            listOf(
                SimpleActionDto(1),
                SimpleActionDto(2),
                SimpleActionDto(3)
            )
        )

        val response = client.post("/api/v1/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(request)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    @DisplayName("Space role exist, user has rights to edit this role, should return OK")
    fun putRoles_roleExistUserHasRights_shouldReturnOk() = webTest {
        //given
        val spaceId = randomSpaceId()
        val roleIdToEdit = initDataHelper.createRole(spaceId)
        val roleAssignedToUser = initDataHelper.createRole(spaceId)
        initDataHelper.apply {
            createRolesActions(
                roleAssignedToUser, 10, mapOf(
                    "manageable_roles" to "[$roleIdToEdit]",
                    "grantable_actions" to "[1,2,3]"
                )
            )
        }
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = roleAssignedToUser, isCreator = false)
        val request = PutRoleRequest(
            "TestRole",
            true,
            listOf(
                SimpleActionDto(1),
                SimpleActionDto(2),
                SimpleActionDto(3)
            )
        )
        //when

        val response = client.put("/api/v1/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            parameter("spaceId", spaceId)
            parameter("roleId", roleIdToEdit)
            setBody(request)
        }
        //then
        assertEquals(HttpStatusCode.OK, response.status)
        val responseDto = response.body<GetEnrichedRoleResponse>()

        assertThat(responseDto)
            .isNotNull
        assertThat(responseDto.actions).hasSize(3)
        assertThat(responseDto.name).isEqualTo("TestRole")
        assertThat(responseDto.isVisible).isEqualTo(true)
    }

    @Test
    @DisplayName("Space role exist, user has no rights to edit this role, should return 403")
    fun putRoles_roleExistUserHasNoRights_shouldReturnForbidden() = webTest {
        //given
        val spaceId = randomSpaceId()
        val roleIdToEdit = initDataHelper.createRole(spaceId)
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = DEFAULT_USER_ROLE_ID, isCreator = false)
        val request = PutRoleRequest(
            "TestRole",
            true,
            listOf(
                SimpleActionDto(1),
                SimpleActionDto(2),
                SimpleActionDto(3)
            )
        )
        //when

        val response = client.put("/api/v1/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            parameter("spaceId", spaceId)
            parameter("roleId", roleIdToEdit)
            setBody(request)
        }
        //then
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    @DisplayName("Space role exist, user has created this role, should return OK")
    fun putRoles_roleExistUserCreatedRole_shouldReturnOk() = webTest {
        //given
        val spaceId = randomSpaceId()
        val roleIdToEdit = initDataHelper.createRole(spaceId, authenticatedUserId)
        val roleAssignedToUser = initDataHelper.createRole(spaceId)
        initDataHelper.apply {
            createRolesActions(
                roleAssignedToUser, 10, mapOf(
                    "grantable_actions" to "[1,2,3]"
                )
            )
        }
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = roleAssignedToUser, isCreator = false)
        val request = PutRoleRequest(
            "TestRole",
            true,
            listOf(
                SimpleActionDto(1),
                SimpleActionDto(2),
                SimpleActionDto(3)
            )
        )
        //when

        val response = client.put("/api/v1/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            parameter("spaceId", spaceId)
            parameter("roleId", roleIdToEdit)
            setBody(request)
        }
        //then
        assertEquals(HttpStatusCode.OK, response.status)
        val responseDto = response.body<GetEnrichedRoleResponse>()

        assertThat(responseDto)
            .isNotNull
        assertThat(responseDto.actions).hasSize(3)
        assertThat(responseDto.name).isEqualTo("TestRole")
        assertThat(responseDto.isVisible).isEqualTo(true)
    }

    @Test
    @DisplayName("Default role exists, user has right to edit this role, but role has GLOBAL scope, should return 403")
    fun putRoles_roleExistWithGlobalScopeUserTriesToEdit_shouldReturn403() = webTest {
        //given
        val spaceId = randomSpaceId()
        val roleAssignedToUser = initDataHelper.createRole(spaceId)
        initDataHelper.apply {
            createRolesActions(
                roleAssignedToUser, 10, mapOf(
                    "manageable_roles" to "[$DEFAULT_USER_ROLE_ID]",
                    "grantable_actions" to "[1,2,3]"
                )
            )
        }
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = roleAssignedToUser, isCreator = false)
        val request = PutRoleRequest(
            "TestRole",
            true,
            listOf(
                SimpleActionDto(1),
                SimpleActionDto(2),
                SimpleActionDto(3)
            )
        )
        //when

        val response = client.put("/api/v1/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            parameter("spaceId", spaceId)
            parameter("roleId", DEFAULT_USER_ROLE_ID)
            setBody(request)
        }
        //then
        assertEquals(HttpStatusCode.Forbidden, response.status)
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
