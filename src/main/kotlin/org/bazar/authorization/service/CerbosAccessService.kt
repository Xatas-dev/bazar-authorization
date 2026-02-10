package org.bazar.authorization.service

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.builders.Principal
import dev.cerbos.sdk.builders.Resource
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*

class CerbosAccessService(
    private val cerbosClient: CerbosBlockingClient,
    private val userSpaceRoleService: UserSpaceRoleService
) {

    /**
     * Checks access using Cerbos.
     * Note: userSpaceRoleService.getUserRole likely queries the DB,
     * so we use newSuspendedTransaction for safe coroutine execution.
     */
    suspend fun checkAccess(userId: UUID, spaceId: Long, action: String): Boolean {
        // 1. Fetch user role (DB call)
        val userRole = suspendTransaction {
            userSpaceRoleService.getUserRole(userId, spaceId)
        }

        // 2. Query Cerbos
        val result = cerbosClient.check(
            Principal.newInstance(userId.toString(), userRole.name),
            Resource.newInstance("space", spaceId.toString()), // Good practice to include resource ID
            action
        )

        return result.isAllowed(action)
    }
}