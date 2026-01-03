package org.bazar.authorization.service

import dev.cerbos.sdk.CerbosBlockingClient
import dev.cerbos.sdk.builders.Principal
import dev.cerbos.sdk.builders.Resource
import org.bazar.authorization.model.authz.enums.AuthorizationAction
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.extensions.toUuid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CerbosAccessService(
    private val cerbosClient: CerbosBlockingClient,
    private val userSpaceRoleService: UserSpaceRoleService
) {

    fun checkAccess(userId: UUID, spaceId: Long, action: AuthorizationAction): Boolean {
        val userRole = userSpaceRoleService.getUserRole(userId, spaceId)
        val result = cerbosClient.check(
            Principal.newInstance(userId.toString(), userRole.name),
            Resource.newInstance("space"),
            action.actionName
        )

        return result.isAllowed(action.actionName)
    }
}