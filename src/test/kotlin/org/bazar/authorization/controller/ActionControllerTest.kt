package org.bazar.authorization.controller

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.bazar.authorization.database.repository.SpaceUserRepository
import org.bazar.authorization.infrastructure.BaseWebTest
import org.bazar.authorization.model.rest.response.ErrorResponse
import org.bazar.authorization.model.rest.response.GetActionsResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.koin.test.inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionControllerTest : BaseWebTest() {

    private val spaceUserRepository: SpaceUserRepository by inject()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    @DisplayName("User has role with access to read all actions hit GET /api/v1/actions, should return ok")
    fun userWithRoleThatCanAccessAllActions_shouldReturnOk() = webTest {
        val spaceId = randomSpaceId()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = 1, false)
        val expectedSize = initDataHelper.getAllActions().size

        val response = client.get("/api/v1/actions?spaceId=$spaceId") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = json.decodeFromString<GetActionsResponse>(response.bodyAsText())
        assertEquals(expectedSize, body.actions.size)
    }

    @Test
    @DisplayName("User has role without access to read all actions hit GET /api/v1/actions, should return 403")
    fun userWithRoleThatCannotAccessAllActions_shouldReturn403() = webTest {
        val spaceId = randomSpaceId()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, roleId = 2, false)

        val response = client.get("/api/v1/actions?spaceId=$spaceId") {
            header(HttpHeaders.Authorization, "Bearer ${authenticatedBearerToken()}")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)

        val body = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals(HttpStatusCode.Forbidden.value, body.code)
        assertTrue(body.message.isNotBlank())
    }

    private fun randomSpaceId(): Long = kotlin.math.abs(java.util.UUID.randomUUID().mostSignificantBits)
}