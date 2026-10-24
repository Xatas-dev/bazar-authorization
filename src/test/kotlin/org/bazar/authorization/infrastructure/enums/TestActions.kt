package org.bazar.authorization.infrastructure.enums

enum class TestActions (
    val actionCode: String,
    val resource: String
) {
    ROLES_MANAGE("MANAGE", "roles"),
    ROLES_ASSIGN("ASSIGN", "roles"),
    ROLES_READ("READ", "roles"),
    CHAT_MESSAGES_READ("READ", "chat_messages"),
    CHAT_MESSAGES_WRITE("WRITE", "chat_messages"),
    STORAGE_NODE_DOWNLOAD("DOWNLOAD", "storage_node")
}