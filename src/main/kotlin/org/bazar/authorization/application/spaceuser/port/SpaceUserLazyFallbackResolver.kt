package org.bazar.authorization.application.spaceuser.port

import org.bazar.authorization.application.spaceuser.port.out.SpaceUserProviderPort
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.spaceuser.SpaceUser
import org.bazar.authorization.infrastructure.util.logger
import java.util.UUID


class SpaceUserLazyFallbackResolver(
    private val spaceUserRepositoryPort: SpaceUserRepositoryPort,
    private val spaceUserProviderPort: SpaceUserProviderPort
) {

    private val logger = logger()

    suspend fun findOrResolveExternally(spaceId: Long, userId: UUID): SpaceUser? {
        return spaceUserRepositoryPort.findBySpaceIdAndUserId(spaceId, userId) ?: run {
            logger.info("User $userId in space $spaceId not found, fallback to bazar-space")
            val fetchedEntity = spaceUserProviderPort.fetchUserBySpaceIdAndUserId(spaceId, userId) ?: run {
                logger.warn("User $userId in space $spaceId not found in bazar-space")
                return null
            }
            spaceUserRepositoryPort.save(fetchedEntity)
            val saved = spaceUserRepositoryPort.findBySpaceIdAndUserId(spaceId, userId)
            return saved
        }
    }


}