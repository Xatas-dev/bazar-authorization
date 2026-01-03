package org.bazar.authorization.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.bazar.authorization.persistence.entity.enums.Role
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "user_space_role")
class UserSpaceRole (
    @EmbeddedId
    val id: UserSpaceRoleId,

    @Column(name = "role", nullable = false, length = 64)
    @Enumerated(EnumType.STRING)
    var role: Role,

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)