package org.bazar.authorization.domain.authz

enum class Permission(val resource: String, val action: String) {
    ROLES_READ("roles", "READ"),
    ROLES_CREATE("roles", "CREATE"),
    ROLES_EDIT("roles", "EDIT"),
    ROLES_ASSIGN("roles", "ASSIGN"),
    SPACE_DELETE("space", "DELETE"),
    SPACE_USER_ADD("space_user", "ADD"),
    SPACE_USER_DELETE("space_user", "DELETE")
}
