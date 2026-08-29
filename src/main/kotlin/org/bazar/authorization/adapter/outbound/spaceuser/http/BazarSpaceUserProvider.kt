package org.bazar.authorization.adapter.outbound.spaceuser.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bazar.authorization.adapter.outbound.http.BazarSpaceHttpClient
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserProviderPort
import java.util.*

class BazarSpaceUserProvider(
    private val bazarSpaceHttpClient: BazarSpaceHttpClient
) : SpaceUserProviderPort {

    override suspend fun fetchUserBySpaceIdAndUserId(
        spaceId: Long,
        userId: UUID
    ) = withContext(Dispatchers.IO) {
        bazarSpaceHttpClient.getSpaceUserInfo(spaceId, userId.toString())?.toSpaceUser()
    }

}