package org.bazar.authorization.service

import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.database.repository.SpaceUserRepository
import org.bazar.authorization.utils.buildSpaceUser
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*

class SpaceUserService(
    private val spaceUserRepository: SpaceUserRepository
) {

    suspend fun deleteAllUsersFromSpace(spaceId: Long) = suspendTransaction {
        spaceUserRepository.deleteUsers(spaceId)
    }

    suspend fun findAllAssignedRoleIdsInSpace(spaceId: Long) = suspendTransaction {
        spaceUserRepository.findAllRoleIds(spaceId)
    }

    suspend fun getOrThrow(spaceId: Long, userId: UUID): SpaceUserEntity = suspendTransaction {
        spaceUserRepository.findRoleIdBySpaceIdAndUserId(spaceId, userId)
            ?: throw ApiException(ApiExceptions.NO_SUCH_USER_IN_SPACE, "spaceId: $spaceId, userId: $userId")
    }

    suspend fun getOrNull(spaceId: Long, userId: UUID): SpaceUserEntity? = suspendTransaction {
        spaceUserRepository.findRoleIdBySpaceIdAndUserId(spaceId, userId)
    }

    suspend fun saveOnConflictThrow(spaceId: Long, userId: UUID, roleId: Long, creator: Boolean) = suspendTransaction {
        val entityToSave = buildSpaceUser(spaceId, userId, roleId, creator)

        spaceUserRepository.findRoleIdBySpaceIdAndUserId(entityToSave.spaceId, entityToSave.userId)
            ?.let { throw ApiException(ApiExceptions.ALREADY_EXISTS) }
            ?: spaceUserRepository.save(entityToSave)

    }

    suspend fun deleteSpaceUser(spaceId: Long, userId: UUID) = suspendTransaction {
        spaceUserRepository.deleteSpaceUser(spaceId, userId)
    }

}