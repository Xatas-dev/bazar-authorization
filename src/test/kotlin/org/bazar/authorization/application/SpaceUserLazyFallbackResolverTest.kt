package org.bazar.authorization.application

import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import org.bazar.authorization.adapter.outbound.http.BazarSpaceHttpClient
import org.bazar.authorization.adapter.outbound.http.BazarSpaceUserResponse
import org.bazar.authorization.application.spaceuser.port.SpaceUserLazyFallbackResolver
import org.bazar.authorization.infrastructure.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.koin.test.inject
import java.util.UUID
import kotlin.math.abs

class SpaceUserLazyFallbackResolverTest: BaseIntegrationTest() {

    private val bazarSpaceClientMock by inject<BazarSpaceHttpClient>()
    private val spaceUserResolver by inject<SpaceUserLazyFallbackResolver>()

    @Test
    @DisplayName("Lazy space user fetch should work as expected if there was no space user locally.")
    fun test_findOrResolveExternally_shouldResolveAndSave_ifNotFound() = integrationTest {
        //given
        val spaceId = randomSpaceId()
        coEvery { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) } returns BazarSpaceUserResponse(
            authenticatedUserId.toString(), spaceId, false
        )

        //when
        val actualResult = spaceUserResolver.findOrResolveExternally(spaceId, authenticatedUserId)

        //then
        val usersInDb = initDataHelper.getAllSpaceUsers(spaceId).filter { spaceUser -> spaceUser.userId == authenticatedUserId }
        assertEquals(1, usersInDb.size)
        val user = usersInDb.first()
        assertEquals(user, actualResult)
        coVerify(exactly = 1) { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) }
    }

    @Test
    @DisplayName("No calls to bazar-space should be done if space user exists locally.")
    fun test_findOrResolveExternally_shouldNotCallBazarSpace_ifFoundLocally() = integrationTest {
        //given
        val spaceId = randomSpaceId()
        initDataHelper.createSpaceUser(spaceId, authenticatedUserId, 1, false)

        //when
        val actualResult = spaceUserResolver.findOrResolveExternally(spaceId, authenticatedUserId)

        //then
        val usersInDb = initDataHelper.getAllSpaceUsers(spaceId).filter { spaceUser -> spaceUser.userId == authenticatedUserId }
        assertEquals(1, usersInDb.size)
        val user = usersInDb.first()
        assertEquals(user, actualResult)
        coVerify(exactly = 0) { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) }
    }

    @Test
    @DisplayName("Null should be returned if space user was not found neither locally nor in bazar-space.")
    fun test_findOrResolveExternally_shouldReturnNull_ifNotFoundAnywhere() = integrationTest {
        //given
        val spaceId = randomSpaceId()
        coEvery { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) } returns null

        //when
        val actualResult = spaceUserResolver.findOrResolveExternally(spaceId, authenticatedUserId)

        //then
        assertNull(actualResult)
        coVerify(exactly = 1) { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) }
    }

    @Test
    @DisplayName("Exception should be thrown if bazar-space returns 200 with broken response.")
    fun test_findOrResolveExternally_shouldThrow_ifBazarSpaceResponseIsBroken() = integrationTest {
        //given
        val spaceId = randomSpaceId()
        coEvery { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) } throws SerializationException("Broken response body")

        //when then
        assertThrows(SerializationException::class.java) {
            runBlocking { spaceUserResolver.findOrResolveExternally(spaceId, authenticatedUserId) }
        }
        coVerify(exactly = 1) { bazarSpaceClientMock.getSpaceUserInfo(any(), any()) }
    }

    private fun randomSpaceId(): Long = abs(UUID.randomUUID().mostSignificantBits)
}