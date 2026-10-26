package org.bazar.authorization.infrastructure

import org.bazar.authorization.adapter.outbound.action.persistence.Actions
import org.bazar.authorization.adapter.outbound.role.persistence.RolesActionsRepositoryAdapter
import org.bazar.authorization.adapter.outbound.spaceuser.persistence.SpaceUsers
import org.bazar.authorization.adapter.outbound.spaceuser.persistence.toSpaceUser
import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.role.port.out.RoleRepositoryPort
import org.bazar.authorization.application.spaceuser.port.out.SpaceUserRepositoryPort
import org.bazar.authorization.domain.role.Role
import org.bazar.authorization.domain.role.RoleActionMapping
import org.bazar.authorization.domain.spaceuser.SpaceUser
import org.bazar.authorization.infrastructure.enums.TestActions
import org.bazar.authorization.infrastructure.enums.TestAttributes
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
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

    fun createRolesActions(
        roleId: Long,
        action: TestActions,
    ) = transaction {
        val actionId = findActionIdRaw(action.actionCode, action.resource)

        rolesActionsRepositoryAdapter.save(
            RoleActionMapping(
                roleId = roleId,
                actionId = actionId
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

    suspend fun getActionIdByTestAction(action: TestActions): Int = suspendTransaction {
        findActionIdRaw(action.actionCode, action.resource)
    }

    private fun findActionIdRaw(code: String, resource: String): Int {
        val id = Actions.selectAll()
            .where { (Actions.code eq code) and (Actions.resource eq resource) }
            .map { it[Actions.id].value }
            .firstOrNull()
        return id ?: throw IllegalArgumentException("No such action in DB actionCode: $code, actionResource: $resource")
    }
}
