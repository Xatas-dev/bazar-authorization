package org.bazar.authorization.adapter.inbound.rest.spaceuser

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import org.bazar.authorization.infrastructure.BaseWebTest
import org.bazar.authorization.adapter.inbound.rest.dto.response.GetRoleNamesResponse
import org.bazar.authorization.adapter.outbound.http.BazarSpaceHttpClient
import org.bazar.authorization.adapter.outbound.http.BazarSpaceUserResponse
import org.bazar.authorization.adapter.outbound.spaceuser.persistence.SpaceUsers.userId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import java.util.*
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SpaceUsersControllerTest : BaseWebTest() {

    val bazarSpaceClientMock by inject<BazarSpaceHttpClient>()

    companion object {
        private const val DEFAULT_USER_ROLE_ID = 1L
    }

    @Test
    @DisplayName("PATCH /space-users/roles. Role exist, user is creator, should return OK")
    fun userIsCreatorTriesToAssignRole_shouldReturnOk() = webTest {
        //given
        val spaceId = randomSpaceId()
        val userIdToAssign = UUID.randomUUID()
        val roleToAssign = initDataHelper.createRole(spaceId)
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, true)
        initDataHelper.createSpaceUser(spaceId, userIdToAssign, DEFAULT_USER_ROLE_ID, false)

        //when
        val response =
            client.patch("/api/v1/space-users/roles?spaceId=$spaceId&userId=$userIdToAssign&roleId=$roleToAssign") {
                header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            }
        //then
        assertEquals(HttpStatusCode.OK, response.status)

        val targetUsersInDb = initDataHelper.getAllSpaceUsers(spaceId).filter { it.userId == userIdToAssign }
        assertEquals(targetUsersInDb.size, 1)
        val targetUserInDb = targetUsersInDb.first()
        assertEquals(targetUserInDb.roleId, roleToAssign)
    }

    @Test
    @DisplayName("PATCH /space-users/roles. Role exist, user has all rights to assign this role, should return OK")
    fun userHasAllRightToAssignRole_shouldReturnOk() = webTest {
        //given
        val spaceId = randomSpaceId()
        val userIdToAssign = UUID.randomUUID()
        val roleToAssign = initDataHelper.createRole(spaceId)
        val requesterRoleId = initDataHelper.createRole(spaceId)
        initDataHelper.apply {
            createRolesActions(requesterRoleId, 9, mapOf("assignable_roles" to "[$roleToAssign]"))
        }
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, requesterRoleId, false)
        initDataHelper.createSpaceUser(spaceId, userIdToAssign, DEFAULT_USER_ROLE_ID, false)

        //when
        val response =
            client.patch("/api/v1/space-users/roles?spaceId=$spaceId&userId=$userIdToAssign&roleId=$roleToAssign") {
                header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            }
        //then
        assertEquals(HttpStatusCode.OK, response.status)

        val targetUsersInDb = initDataHelper.getAllSpaceUsers(spaceId).filter { it.userId == userIdToAssign }
        assertEquals(targetUsersInDb.size, 1)
        val targetUserInDb = targetUsersInDb.first()
        assertEquals(targetUserInDb.roleId, roleToAssign)
    }

    @Test
    @DisplayName("PATCH /space-users/roles. Role exist, user has no rights to assign this role, should return FORBIDDEN")
    fun userHasNoAccessToAssignRole_shouldReturnForbidden() = webTest {
        //given
        val spaceId = randomSpaceId()
        val userIdToAssign = UUID.randomUUID()
        val roleToAssign = initDataHelper.createRole(spaceId)
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, DEFAULT_USER_ROLE_ID, false)
        initDataHelper.createSpaceUser(spaceId, userIdToAssign, DEFAULT_USER_ROLE_ID, false)

        //when
        val response =
            client.patch("/api/v1/space-users/roles?spaceId=$spaceId&userId=$userIdToAssign&roleId=$roleToAssign") {
                header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            }
        //then
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    @DisplayName("Requester is space participant, GET /space-users/roles should return expected OK response")
    fun getRoleNames_shouldReturnOk() = webTest {
        //given
        val spaceId = randomSpaceId()
        val userIds = List(5) { UUID.randomUUID() }
        val roleIds = List(3) { initDataHelper.createRole(spaceId) }

        val userIdToRoleIdMap = userIds.mapIndexed { index, userId ->
            userId to roleIds.getOrElse(index) { 1L }
        }.toMap()

        userIdToRoleIdMap.forEach {
            initDataHelper.createSpaceUser(spaceId, it.key, it.value, isCreator = false)
        }

        initDataHelper.createSpaceUser(spaceId, authenticatedUserId,
            DEFAULT_USER_ROLE_ID, isCreator = true)

        //when
        val response = client.get("/api/v1/space-users/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            parameter("spaceId", spaceId)
            userIds.forEach { userId -> parameter("userIds", userId) }
        }
        //then
        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 0) { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) }
        assertEquals(5, response.body<GetRoleNamesResponse>().roles.size)
    }

    @Test
    @DisplayName("GET /space-users/roles. No space user locally, should fetch from bazar-space, persist and return 200")
    fun getRoleNames_noUserLocally_shouldFetchFromBazarSpaceAndPersist() = webTest {
        //given
        val spaceId = randomSpaceId()
        val userIds = List(5) { UUID.randomUUID() }
        val roleIds = List(3) { initDataHelper.createRole(spaceId) }

        val userIdToRoleIdMap = userIds.mapIndexed { index, userId ->
            userId to roleIds.getOrElse(index) { DEFAULT_USER_ROLE_ID }
        }.toMap()

        userIdToRoleIdMap.forEach {
            initDataHelper.createSpaceUser(spaceId, it.key, it.value, isCreator = false)
        }

        coEvery { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) } returns BazarSpaceUserResponse(
            authenticatedUserId.toString(), spaceId , false
        )

        //when
        val response = client.get("/api/v1/space-users/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            parameter("spaceId", spaceId)
            userIds.forEach { userId -> parameter("userIds", userId) }
        }

        //then
        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) }

        val persistedRequester = initDataHelper.getAllSpaceUsers(spaceId).filter { it.userId == authenticatedUserId }
        assertEquals(1, persistedRequester.size)
        val persistedUser = persistedRequester.first()
        assertEquals(DEFAULT_USER_ROLE_ID, persistedUser.roleId)
        assertFalse(persistedUser.isCreator)
    }

    @Test
    @DisplayName("GET /space-users/roles. No space user locally and in bazar-space, should return 403")
    fun getRoleNames_noUserAnywhere_shouldReturnForbidden() = webTest {
        //given
        val spaceId = randomSpaceId()
        val userIds = List(2) { UUID.randomUUID() }

        coEvery { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) } returns null

        //when
        val response = client.get("/api/v1/space-users/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            parameter("spaceId", spaceId)
            userIds.forEach { userId -> parameter("userIds", userId) }
        }

        //then
        coVerify(exactly = 1) { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    private fun randomSpaceId(): Long = abs(UUID.randomUUID().mostSignificantBits)
}