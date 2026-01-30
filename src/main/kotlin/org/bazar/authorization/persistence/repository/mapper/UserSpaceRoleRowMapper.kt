package org.bazar.authorization.persistence.repository.mapper

import org.bazar.authorization.persistence.entity.UserSpaceRole
import org.bazar.authorization.persistence.entity.enums.Role
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.*

@Component
class UserSpaceRoleRowMapper : RowMapper<UserSpaceRole> {
    override fun mapRow(
        rs: ResultSet,
        rowNum: Int
    ): UserSpaceRole =
        UserSpaceRole(
            spaceId = rs.getLong("space_id"),
            userId = rs.getObject("user_id", UUID::class.java),
            role = Role.valueOf(rs.getString("role")),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at").toInstant()
        )
}