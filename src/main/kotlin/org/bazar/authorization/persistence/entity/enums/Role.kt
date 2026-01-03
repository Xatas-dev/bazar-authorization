package org.bazar.authorization.persistence.entity.enums

enum class Role {
    CREATOR, MEMBER;

    companion object {
        fun contains(role: String) = Role.entries
            .map { it.name }
            .contains(role)
    }
}

