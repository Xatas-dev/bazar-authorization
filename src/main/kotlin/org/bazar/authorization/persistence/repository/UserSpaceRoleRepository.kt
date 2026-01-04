package org.bazar.authorization.persistence.repository

import jakarta.transaction.Transactional
import org.bazar.authorization.persistence.entity.UserSpaceRole
import org.bazar.authorization.persistence.entity.UserSpaceRoleId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
@Transactional
interface UserSpaceRoleRepository : JpaRepository<UserSpaceRole, UserSpaceRoleId> {

    @Query(
        """
        delete from UserSpaceRole usr
        where usr.id.spaceId = :spaceId
    """
    )
    @Modifying
    fun deleteAllBySpaceId(spaceId: Long): Int

}