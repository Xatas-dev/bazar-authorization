package org.bazar.authorization.controller

import io.ktor.client.request.*
import io.ktor.http.*
import org.bazar.authorization.infrastructure.BaseWebTest
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

    private fun randomSpaceId(): Long = abs(UUID.randomUUID().mostSignificantBits)
}