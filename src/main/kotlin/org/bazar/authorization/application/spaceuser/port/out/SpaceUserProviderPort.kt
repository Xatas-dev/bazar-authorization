package org.bazar.authorization.application.spaceuser.port.out

import org.bazar.authorization.domain.spaceuser.SpaceUser
import java.util.UUID

interface SpaceUserProviderPort {

    suspend fun fetchUserBySpaceIdAndUserId(spaceId: Long, userId: UUID): SpaceUser?

}