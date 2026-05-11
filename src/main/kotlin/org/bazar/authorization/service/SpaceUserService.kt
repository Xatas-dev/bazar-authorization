package org.bazar.authorization.service

import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.database.repository.SpaceUserRepository
import org.bazar.authorization.utils.extensions.builder.buildSpaceUser
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.exceptions.ApiExceptions.NO_SUCH_USER_IN_SPACE
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

    suspend fun getSpaceUser(spaceId: Long, userId: UUID): SpaceUserEntity = suspendTransaction {
        spaceUserRepository.findRoleIdBySpaceIdAndUserId(spaceId, userId)
            ?: throw ApiException(NO_SUCH_USER_IN_SPACE, "spaceId: $spaceId, userId: $userId")
    }

    suspend fun getOrNull(spaceId: Long, userId: UUID): SpaceUserEntity? = suspendTransaction {
        spaceUserRepository.findRoleIdBySpaceIdAndUserId(spaceId, userId)
    }

    suspend fun saveOnConflictThrow(spaceId: Long, userId: UUID, roleId: Long, isCreator: Boolean) = suspendTransaction {
        val entityToSave = buildSpaceUser(spaceId, userId, roleId, isCreator)

        spaceUserRepository.findRoleIdBySpaceIdAndUserId(entityToSave.spaceId, entityToSave.userId)
            ?.let { throw ApiException(ApiExceptions.USER_ALREADY_EXISTS) }
            ?: spaceUserRepository.save(entityToSave)

    }

    suspend fun assignRoleToUser(userId: UUID, spaceId: Long, roleId: Long) = suspendTransaction {
        val existingUser = spaceUserRepository.findRoleIdBySpaceIdAndUserId(spaceId, userId)
            ?: throw ApiException(NO_SUCH_USER_IN_SPACE, "userId: $userId, spaceId: $spaceId")

        existingUser.roleId = roleId
        spaceUserRepository.save(existingUser)
    }

    suspend fun deleteSpaceUser(spaceId: Long, userId: UUID) = suspendTransaction {
        spaceUserRepository.deleteSpaceUser(spaceId, userId)
    }

    suspend fun getAllUsers(spaceId: Long, userIds: List<UUID>) = suspendTransaction {
        spaceUserRepository.findAllBySpaceIdAndUserIdsIn(spaceId, userIds)
    }

}