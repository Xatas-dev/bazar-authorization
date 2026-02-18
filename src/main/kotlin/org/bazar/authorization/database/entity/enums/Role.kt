package org.bazar.authorization.database.entity.enums

enum class Role {
    CREATOR, MEMBER;

    companion object {
        fun contains(role: String) = entries
            .map { it.name }
            .contains(role)
    }
}

