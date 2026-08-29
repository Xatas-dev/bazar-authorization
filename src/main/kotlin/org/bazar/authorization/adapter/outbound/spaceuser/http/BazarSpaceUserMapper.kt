package org.bazar.authorization.adapter.outbound.spaceuser.http

import org.bazar.authorization.adapter.outbound.http.BazarSpaceUserResponse
import org.bazar.authorization.domain.spaceuser.SpaceUser
import java.util.UUID

fun BazarSpaceUserResponse.toSpaceUser() =
    SpaceUser.create(spaceId, UUID.fromString(userId), creator)
