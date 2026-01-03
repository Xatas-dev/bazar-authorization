package org.bazar.authorization.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.hibernate.Hibernate
import java.io.Serializable
import java.util.*
import kotlin.uuid.Uuid

@Embeddable
class UserSpaceRoleId (
    @Column(name = "space_id", nullable = false)
    val spaceId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: UUID
) : Serializable {

    override fun hashCode(): Int = Objects.hash(spaceId, userId)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

        other as UserSpaceRoleId

        return spaceId == other.spaceId &&
                userId == other.userId
    }

    override fun toString(): String {
        return "UserSpaceRoleId {spaceId = $spaceId, userId = $userId}"
    }

    companion object {
        private const val serialVersionUID = 0L
    }
}