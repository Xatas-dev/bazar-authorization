package org.bazar.authorization.adapter.inbound.rest.role

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.bazar.authorization.adapter.inbound.rest.dto.request.CreateRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.request.PutRoleRequest
import org.bazar.authorization.adapter.inbound.rest.dto.request.SimpleActionDto
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetEnrichedRoleResponse
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetRolesResponse
import org.bazar.authorization.infrastructure.BaseWebTest
import org.bazar.authorization.infrastructure.enums.TestActions
import org.bazar.authorization.infrastructure.enums.TestAttributes
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
    @DisplayName("Create role, happy path")
    fun createRole_withAllowedActions_shouldReturnOk() = webTest {
        //given
        val spaceId = randomSpaceId()
        val newRoleName = "TestRole"
        val requesterRoleId = initDataHelper.createRole(spaceId)

        initDataHelper.createRolesActions(
            roleId = requesterRoleId,
            action = TestActions.CHAT_MESSAGES_READ,
        )

        initDataHelper.createRolesActions(
            roleId = requesterRoleId,
            action = TestActions.ROLES_MANAGE
        )

        initDataHelper.createRolesActions(
            roleId = requesterRoleId,
            action = TestActions.ROLES_ASSIGN
        )

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = requesterRoleId, isCreator = false)

        val request = CreateRoleRequest(
            spaceId,
            newRoleName,
            true,
            listOf(
                SimpleActionDto(initDataHelper.getActionIdByTestAction(TestActions.CHAT_MESSAGES_READ)),
                SimpleActionDto(
                    initDataHelper.getActionIdByTestAction(TestActions.ROLES_MANAGE)
                ),
                SimpleActionDto(
                    initDataHelper.getActionIdByTestAction(TestActions.ROLES_ASSIGN)
                ),
            )
        )

        //when
        val response = client.post("/api/v1/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(request)
        }
        //then
        assertEquals(HttpStatusCode.OK, response.status)

        val responseBody = response.body<GetEnrichedRoleResponse>()

        assertThat(responseBody.spaceId).isEqualTo(spaceId)
        assertThat(responseBody.name).isEqualTo(newRoleName)
        assertThat(responseBody.actions).hasSize(3)

        assertThat(responseBody.actions)
            .extracting(
                { it.resource },
                { it.code },
            ).containsExactlyInAnyOrder(
                tuple(
                    TestActions.ROLES_MANAGE.resource, TestActions.ROLES_MANAGE.actionCode
                ),
                tuple(
                    TestActions.ROLES_ASSIGN.resource, TestActions.ROLES_ASSIGN.actionCode
                ),
                tuple(
                    TestActions.CHAT_MESSAGES_READ.resource,
                    TestActions.CHAT_MESSAGES_READ.actionCode
                )
            )

    }


    @Test
    @DisplayName("Requester has a role with actions: chat_messages:READ tries to create a role, should return 403")
    fun createRole_withForbiddenAction_shouldReturnForbidden() = webTest {
        val spaceId = randomSpaceId()

        val requesterRoleId = initDataHelper.createRole(spaceId)
        initDataHelper.createRolesActions(
            roleId = requesterRoleId,
            action = TestActions.CHAT_MESSAGES_READ
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
    @DisplayName("Edit role, happy path")
    fun putRoles_roleExistUserHasRights_shouldReturnOk() = webTest {
        //given
        val spaceId = randomSpaceId()
        val roleIdToEdit = initDataHelper.createRole(spaceId)
        initDataHelper.apply {
            createRolesActions(roleIdToEdit, TestActions.STORAGE_NODE_DOWNLOAD)
            createRolesActions(
                roleIdToEdit, TestActions.ROLES_MANAGE
            )
        }
        val roleAssignedToUser = initDataHelper.createRole(spaceId)
        initDataHelper.apply {

            createRolesActions(
                roleAssignedToUser, TestActions.ROLES_MANAGE
            )

            createRolesActions(roleAssignedToUser, TestActions.CHAT_MESSAGES_WRITE)
            createRolesActions(roleAssignedToUser, TestActions.CHAT_MESSAGES_READ)
        }
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = roleAssignedToUser, isCreator = false)

        val request = PutRoleRequest(
            "TestRole",
            true,
            listOf(
                SimpleActionDto(initDataHelper.getActionIdByTestAction(TestActions.CHAT_MESSAGES_WRITE)),
                SimpleActionDto(initDataHelper.getActionIdByTestAction(TestActions.CHAT_MESSAGES_READ)),
                SimpleActionDto(initDataHelper.getActionIdByTestAction(TestActions.STORAGE_NODE_DOWNLOAD)),
                SimpleActionDto(
                    initDataHelper.getActionIdByTestAction(TestActions.ROLES_MANAGE)
                ),
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
        assertThat(responseDto.actions).hasSize(4)
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
    @DisplayName("Default role exists, user has right to edit this role, but role has GLOBAL scope, should return 403")
    fun putRoles_roleExistWithGlobalScopeUserTriesToEdit_shouldReturn403() = webTest {
        //given
        val spaceId = randomSpaceId()
        val roleAssignedToUser = initDataHelper.createRole(spaceId)
        initDataHelper.apply {
            createRolesActions(
                roleAssignedToUser, TestActions.ROLES_MANAGE
            )

            createRolesActions(
                roleAssignedToUser, TestActions.CHAT_MESSAGES_READ
            )
        }
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = roleAssignedToUser, isCreator = false)
        val request = PutRoleRequest(
            "TestRole",
            true,
            listOf(
                SimpleActionDto(initDataHelper.getActionIdByTestAction(TestActions.CHAT_MESSAGES_READ))
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

    @Test
    @DisplayName("Edit a role, manageable_roles attribute is not a subset creator's manageable_roles, should return 403")
    fun editRole_withBadManageableRoles_shouldReturnForbidden() = webTest {
        val spaceId = randomSpaceId()

        val roleIdToEdit = initDataHelper.createRole(spaceId)
        initDataHelper.apply {
            createRolesActions(roleIdToEdit, TestActions.STORAGE_NODE_DOWNLOAD)
            initDataHelper.createRolesActions(
                roleId = roleIdToEdit,
                action = TestActions.ROLES_MANAGE
            )
        }

        val requesterRoleId = initDataHelper.createRole(spaceId)
        initDataHelper.createRolesActions(
            roleId = requesterRoleId,
            action = TestActions.ROLES_MANAGE
        )

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = requesterRoleId, isCreator = false)

        val request = PutRoleRequest(
            "TestRole",
            true,
            listOf(
                SimpleActionDto(
                    initDataHelper.getActionIdByTestAction(TestActions.ROLES_MANAGE)
                )
            )
        )

        val response = client.put("/api/v1/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            parameter("spaceId", spaceId)
            parameter("roleId", roleIdToEdit)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
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
