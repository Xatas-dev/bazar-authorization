package org.bazar.authorization.persistence.repository

import jakarta.transaction.Transactional
import org.bazar.authorization.persistence.entity.UserSpaceRole
import org.bazar.authorization.persistence.entity.UserSpaceRoleId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
@Transactional
interface UserSpaceRoleRepository: JpaRepository<UserSpaceRole, UserSpaceRoleId> {
}