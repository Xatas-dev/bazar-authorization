package org.bazar.authorization.service.api

import org.bazar.authorization.service.SpaceUserService
import org.bazar.authorization.utils.logger
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*

class SpaceUserApiService(
    private val spaceUserService: SpaceUserService
) {
    private val logger = logger()

    suspend fun assignRoleToUser(spaceId: Long, userId: UUID, roleId: Long) = suspendTransaction {
        spaceUserService.assignRoleToUser(userId, spaceId, roleId)
    }

}
