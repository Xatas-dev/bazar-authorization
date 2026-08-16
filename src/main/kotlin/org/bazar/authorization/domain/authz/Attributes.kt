package org.bazar.authorization.domain.authz

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
