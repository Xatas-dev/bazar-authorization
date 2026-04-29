package org.bazar.authorization.utils.authorization.enums

enum class Permission(val resource: String, val action: String) {
    READ_ACTIONS("space_user_actions", "READ"),
    WRITE_ACTIONS("space_user_actions", "WRITE")
}