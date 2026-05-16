package org.bazar.authorization.controller

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import org.bazar.authorization.infrastructure.BaseWebTest
import org.bazar.authorization.model.rest.response.GetRoleNamesResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.math.abs
import kotlin.test.assertEquals

class SpaceUsersControllerTest : BaseWebTest() {


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
        assertEquals(5, response.body<GetRoleNamesResponse>().roles.size)
    }

    @Test
    @DisplayName("Requester is NOT space participant, GET /space-users/roles should return 403")
    fun getRoleNames_shouldReturnForbidden() = webTest {
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


        //when
        val response = client.get("/api/v1/space-users/roles") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            parameter("spaceId", spaceId)
            userIds.forEach { userId -> parameter("userIds", userId) }
        }
        //then
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    private fun randomSpaceId(): Long = abs(UUID.randomUUID().mostSignificantBits)
}