package org.bazar.authorization.persistence.repository

import org.bazar.authorization.persistence.entity.UserSpaceRole
import org.bazar.authorization.persistence.repository.mapper.UserSpaceRoleRowMapper
import org.bazar.authorization.utils.extensions.toTimestamp
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
class UserSpaceRoleRepository(
    private val jdbcClient: JdbcClient,
    private val userSpaceRoleRowMapper: UserSpaceRoleRowMapper
) {

    @Transactional
    fun save(userSpaceRole: UserSpaceRole): Int {
        return jdbcClient.sql(INSERT)
            .param("spaceId", userSpaceRole.spaceId)
            .param("userId", userSpaceRole.userId)
            .param("role", userSpaceRole.role.name)
            .param("createdAt", userSpaceRole.createdAt.toTimestamp())
            .param("updatedAt", userSpaceRole.updatedAt.toTimestamp())
            .update()
    }

    @Transactional(readOnly = true)
    fun findById(spaceId: Long, userId: UUID): UserSpaceRole? {
        return jdbcClient.sql(FIND_BY_SPACE_ID_USER_ID)
            .param("spaceId", spaceId)
            .param("userId", userId)
            .query(userSpaceRoleRowMapper).optional().orElse(null)
    }

    @Transactional
    fun deleteBySpaceIdAndUserId(spaceId: Long, userId: UUID): Int {
        return jdbcClient.sql(DELETE_BY_SPACE_ID_AND_USER_ID)
            .param("spaceId", spaceId)
            .param("userId", userId)
            .update()
    }

    @Transactional
    fun deleteAllBySpaceId(spaceId: Long): Int {
        return jdbcClient.sql(DELETE_ALL_BY_SPACE_ID)
            .param("spaceId", spaceId)
            .update()
    }

    @Transactional(readOnly = true)
    fun findAll(): Collection<UserSpaceRole> {
        return jdbcClient.sql(FIND_ALL)
            .query(userSpaceRoleRowMapper)
            .set()
    }

    companion object {
        private val INSERT = """
        INSERT INTO user_space_role (space_id, user_id, role, created_at, updated_at)
        VALUES (:spaceId, :userId, :role, :createdAt, :updatedAt)
        ON CONFLICT (space_id, user_id) 
        DO UPDATE SET 
        role = EXCLUDED.role,
        updated_at = EXCLUDED.updated_at
    """.trimIndent()

        private val FIND_BY_SPACE_ID_USER_ID = """
            SELECT *
            FROM user_space_role 
            WHERE space_id = :spaceId AND user_id = :userId
    """.trimIndent()

        private val DELETE_BY_SPACE_ID_AND_USER_ID = """
            DELETE FROM user_space_role WHERE space_id = :spaceId AND user_id = :userId
        """.trimIndent()

        private val DELETE_ALL_BY_SPACE_ID = """
            DELETE FROM user_space_role WHERE space_id = :spaceId
        """.trimIndent()

        private val FIND_ALL = """
            SELECT *
            FROM user_space_role
        """.trimIndent()
    }
}