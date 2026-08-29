package org.bazar.authorization.application.spaceuser.port.`in`.impl

import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.spaceuser.port.`in`.GetRoleNamesUseCase
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.application.spaceuser.query.GetRoleNamesQuery
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException
import org.bazar.authorization.application.spaceuser.UserRoleName
import org.bazar.authorization.application.spaceuser.port.SpaceUserLazyFallbackResolver
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GetRoleNamesUseCaseImpl(
    private val spaceUserRepositoryPort: SpaceUserRepositoryPort,
    private val roleRepositoryPort: RoleRepositoryPort,
    private val spaceUserResolver: SpaceUserLazyFallbackResolver
) : GetRoleNamesUseCase {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    override suspend fun execute(query: GetRoleNamesQuery): List<UserRoleName> = suspendTransaction {
        spaceUserResolver.findOrResolveExternally(query.spaceId, query.requesterId)
            ?: throw DomainException(
                DomainErrors.NO_SUCH_USER_IN_SPACE,
                "spaceId: ${query.spaceId}, userId: ${query.requesterId}"
            )

        val users = spaceUserRepositoryPort.findAllBySpaceIdAndUserIdsIn(query.spaceId, query.userIds)

        val rolesMap = roleRepositoryPort.findByIdsIn(users.map { it.roleId }.distinct())
            .associateBy { it.id!! }

        users.mapNotNull { user ->
            val role = rolesMap[user.roleId]

            if (role != null) {
                UserRoleName(user, role)
            } else {
                logger.warn("roleId=${user.roleId} not found for user ${user.userId}")
                null
            }
        }
    }
}
