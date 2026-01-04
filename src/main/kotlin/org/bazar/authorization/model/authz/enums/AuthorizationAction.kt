package org.bazar.authorization.model.authz.enums

enum class AuthorizationAction (val actionName: String) {
    ADD_USER_TO_SPACE("add_user_to_space"),
    REMOVE_USER_FROM_SPACE("remove_user_from_space"),
    ACCESS_CHAT("access_chat"),
    DELETE_SPACE("delete_space"),
    ACCESS_STORAGE("access_storage");

}