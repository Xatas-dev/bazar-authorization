package org.bazar.authorization.infrastructure

import org.bazar.authorization.adapter.outbound.role.persistence.RolesActionsRepositoryAdapter
import org.bazar.authorization.adapter.outbound.spaceuser.persistence.SpaceUsers
import org.bazar.authorization.adapter.outbound.spaceuser.persistence.toSpaceUser
import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.application.role.RoleActionMapping
import org.bazar.authorization.domain.spaceuser.SpaceUser
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.test.KoinTest
import java.util.UUID

class InitDataHelper(
    private val spaceUserRepositoryPort: SpaceUserRepositoryPort,
    private val rolesActionsRepositoryAdapter: RolesActionsRepositoryAdapter,
    private val actionRepositoryPort: ActionRepositoryPort,
    private val roleRepositoryPort: RoleRepositoryPort
) : KoinTest {

    suspend fun createSpaceUser(spaceId: Long, userId: UUID, roleId: Long, isCreator: Boolean) {
        spaceUserRepositoryPort.save(SpaceUser.create(spaceId, userId, isCreator, roleId))
    }

    fun createRolesActions(roleId: Long, actionId: Int) = transaction {
        rolesActionsRepositoryAdapter.save(RoleActionMapping(roleId = roleId, actionId = actionId))
    }

    fun createRolesActions(
        roleId: Long,
        actionId: Int,
        assignedAttributes: Map<String, String>
    ) = transaction {
        rolesActionsRepositoryAdapter.save(
            RoleActionMapping(
                roleId = roleId,
                actionId = actionId,
                assignedAttributes = assignedAttributes
            )
        )
    }

    fun getAllActions() = transaction {
        actionRepositoryPort.findAll()
    }

    fun createRole(spaceId: Long, createdBy: UUID = UUID.randomUUID()): Long = transaction {
        roleRepositoryPort.save(Role.createSpaceScoped("rand", true, createdBy, spaceId)).id!!
    }

    fun getAllSpaceUsers(spaceId: Long): List<SpaceUser> {
        return transaction {
            SpaceUsers.selectAll()
                .where { SpaceUsers.spaceId eq spaceId }
                .map { it.toSpaceUser() }
        }
    }
}
