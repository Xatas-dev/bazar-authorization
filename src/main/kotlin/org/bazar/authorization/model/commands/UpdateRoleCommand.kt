package org.bazar.authorization.model.commands

data class UpdateRoleCommand(
    val roleId: Long,
    val name: String,
    val isVisible: Boolean,
    val actionIdToAttributes: Map<Int, Map<String, String>?>
)
