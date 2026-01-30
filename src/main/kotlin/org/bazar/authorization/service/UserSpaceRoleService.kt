package org.bazar.authorization.service

import org.bazar.authorization.persistence.entity.UserSpaceRole
import org.bazar.authorization.persistence.entity.enums.Role
import org.bazar.authorization.persistence.repository.UserSpaceRoleRepository
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions.NO_SUCH_USER_IN_SPACE
import org.bazar.authorization.utils.exceptions.ApiExceptions.SPACE_ALREADY_HAS_CREATOR
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
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
                "space $spaceId has userId $userId as CREATOR" // Logic kept from your original code
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

    fun removeUserFromSpace(userId: UUID, spaceId: Long): Boolean {
        val rowsDeleted = userSpaceRoleRepository.deleteBySpaceIdAndUserId(spaceId, userId)
        return rowsDeleted > 0
    }

    @Transactional(readOnly = true)
    fun getUserRole(userId: UUID, spaceId: Long): Role {
        val userSpaceRole = userSpaceRoleRepository.findById(spaceId, userId)
            ?: throw ApiException(NO_SUCH_USER_IN_SPACE, "userId = $userId, spaceId = $spaceId")

        return userSpaceRole.role
    }
}