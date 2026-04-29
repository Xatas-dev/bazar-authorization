package org.bazar.authorization.infrastructure

import org.bazar.authorization.database.entity.RolesActionsEntity
import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.database.repository.ActionRepository
import org.bazar.authorization.database.repository.RoleRepository
import org.bazar.authorization.database.repository.RolesActionsRepository
import org.bazar.authorization.database.repository.SpaceUserRepository
import org.bazar.authorization.database.tables.SpaceUsers
import org.bazar.authorization.utils.buildRole
import org.bazar.authorization.utils.buildRolesActions
import org.bazar.authorization.utils.buildSpaceUser
import org.bazar.authorization.utils.extensions.toSpaceUserEntity
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.test.KoinTest
import java.util.*

class InitDataHelper(
    private val spaceUserRepository: SpaceUserRepository,
    private val rolesActionsRepository: RolesActionsRepository,
    private val actionRepository: ActionRepository,
    private val roleRepository: RoleRepository,

    ) : KoinTest {

    fun createSpaceUser(spaceId: Long, userId: UUID, roleId: Long, creator: Boolean) = transaction {
        spaceUserRepository.save(buildSpaceUser(spaceId, userId, roleId, creator))
    }

    fun createRolesActions(roleId: Long, actionId: Int) = transaction {
        rolesActionsRepository.save(buildRolesActions(roleId, actionId))
    }

    fun createRolesActions(
        roleId: Long,
        actionId: Int,
        assignedAttributes: Map<String, String>
    ) = transaction {
        rolesActionsRepository.save(
            RolesActionsEntity(
                roleId = roleId,
                actionId = actionId,
                assignedAttributes = assignedAttributes
            )
        )
    }


    fun getAllActions() = transaction {
        actionRepository.getAllActions()
    }

    fun getActionId(code: String, resource: String) = transaction {
        actionRepository.findByCodeAndResource(code, resource)!!.id
    }

    fun createRole(): Long = transaction {
        roleRepository.save(buildRole(RoleScope.USER)).id!!
    }

    fun getAllSpaceUsers(spaceId: Long): List<SpaceUserEntity> {
        return transaction {
            SpaceUsers.selectAll()
                .where { SpaceUsers.spaceId eq spaceId }
                .map { it.toSpaceUserEntity() }
        }
    }

}