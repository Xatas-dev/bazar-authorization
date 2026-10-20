package org.bazar.authorization.domain.authz

/**
Hard-coded constants that are used in bazar-authorization for authorizing incoming requests:
space, space_user resource actions for bazar-space -> bazar-authorization integration
roles for authorizing incoming bazar-authorization API reqeusts
 **/
enum class AuthorizationAction(val resource: String, val action: String) {
    ROLES_READ("roles", "READ"),
    ROLES_MANAGE("roles", "MANAGE"),
    ROLES_ASSIGN("roles", "ASSIGN"),
    SPACE_DELETE("space", "DELETE"),
    SPACE_USER_DELETE("space_user", "DELETE")
}

object PrincipalAttributes {
    const val USER_ID = "user_id"
    const val ROLE_ID = "role_id"
    const val ALLOWED_ACTIONS = "allowed_actions"
    const val IS_CREATOR = "is_creator"
    const val SCOPE = "scope"
    const val ACTIONS_TO_GRANT = "actions_to_grant"
}

object ResourceAttributes {
    const val CREATED_BY = "created_by"
    const val SCOPE = "scope"
    const val ID = "id"
    const val IS_CREATOR = "is_creator"
}
