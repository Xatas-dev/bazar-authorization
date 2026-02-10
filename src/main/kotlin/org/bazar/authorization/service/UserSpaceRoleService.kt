package org.bazar.authorization.service

import org.bazar.authorization.database.entity.UserSpaceRole
import org.bazar.authorization.database.entity.enums.Role
import org.bazar.authorization.database.repository.UserSpaceRoleRepository
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions
import org.bazar.authorization.utils.exceptions.ApiExceptions.NO_SUCH_USER_IN_SPACE
import org.bazar.authorization.utils.exceptions.ApiExceptions.SPACE_ALREADY_HAS_CREATOR
import java.util.*

class UserSpaceRoleService(
    private val userSpaceRoleRepository: UserSpaceRoleRepository
) {

    fun deleteAllFromSpace(spaceId: Long) {
        userSpaceRoleRepository.deleteAllBySpaceId(spaceId)
    }

    fun saveOrUpdateRoleInSpace(userId: UUID, spaceId: Long, role: Role) {
        val entity = UserSpaceRole(
            spaceId = spaceId,
            userId = userId,
            role = role
        )
        userSpaceRoleRepository.save(entity)
    }

    fun createSpaceOwner(userId: UUID, spaceId: Long) {
        val existingRole = userSpaceRoleRepository.findById(spaceId, userId)

        if (existingRole != null) {
            throw ApiException(
                SPACE_ALREADY_HAS_CREATOR,
                "space $spaceId has userId $userId as CREATOR"
            )
        }

        userSpaceRoleRepository.save(
            UserSpaceRole(
                spaceId = spaceId,
                userId = userId,
                role = Role.CREATOR
            )
        )
    }

    fun removeUserFromSpace(userId: UUID, spaceId: Long) {
        val rowsDeleted = userSpaceRoleRepository.deleteBySpaceIdAndUserId(spaceId, userId)
        if (rowsDeleted <= 0)
            throw ApiException(NO_SUCH_USER_IN_SPACE, "userId = $userId, spaceId = $spaceId")
    }

    fun getUserRole(userId: UUID, spaceId: Long): Role {
        val userSpaceRole = userSpaceRoleRepository.findById(spaceId, userId)
            ?: throw ApiException(NO_SUCH_USER_IN_SPACE, "userId = $userId, spaceId = $spaceId")

        return userSpaceRole.role
    }
}