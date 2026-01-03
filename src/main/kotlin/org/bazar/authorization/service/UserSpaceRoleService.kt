package org.bazar.authorization.service

import jakarta.transaction.Transactional
import org.bazar.authorization.persistence.entity.UserSpaceRole
import org.bazar.authorization.persistence.entity.UserSpaceRoleId
import org.bazar.authorization.persistence.entity.enums.Role
import org.bazar.authorization.persistence.repository.UserSpaceRoleRepository
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.exceptions.ApiExceptions.NO_SUCH_USER_IN_SPACE
import org.bazar.authorization.utils.exceptions.ApiExceptions.SPACE_ALREADY_HAS_CREATOR
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@Transactional
class UserSpaceRoleService(
    private val userSpaceRoleRepository: UserSpaceRoleRepository
) {

    fun saveOrUpdateRoleInSpace(userId: UUID, spaceId: Long, role: Role) {
        userSpaceRoleRepository.save(
            UserSpaceRole(
                UserSpaceRoleId(spaceId, userId),
                role
            )
        )
    }

    fun createSpaceOwner(userId: UUID, spaceId: Long){
        if (userSpaceRoleRepository.existsById(UserSpaceRoleId(spaceId = spaceId, userId = userId)))
            throw ApiException(SPACE_ALREADY_HAS_CREATOR, "space $spaceId has userId $userId as CREATOR")

        userSpaceRoleRepository.save(
            UserSpaceRole(
                UserSpaceRoleId(spaceId, userId),
                Role.CREATOR
            )
        )
    }

    fun removeUserFromSpace(userId: UUID, spaceId: Long): Boolean {
        userSpaceRoleRepository.deleteById(UserSpaceRoleId(spaceId, userId))
        return true
    }

    fun getUserRole(userId: UUID, spaceId: Long): Role =
        userSpaceRoleRepository.findById(UserSpaceRoleId(spaceId, userId))
            .orElseThrow { ApiException(NO_SUCH_USER_IN_SPACE, "userId = $userId, spaceId = $spaceId") }
            .role
}