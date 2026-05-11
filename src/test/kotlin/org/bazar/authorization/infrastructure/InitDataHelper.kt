package org.bazar.authorization.infrastructure

import org.bazar.authorization.database.entity.RoleEntity
import org.bazar.authorization.database.entity.RolesActionsEntity
import org.bazar.authorization.database.entity.SpaceUserEntity
import org.bazar.authorization.database.entity.enums.RoleScope
import org.bazar.authorization.database.repository.ActionRepository
import org.bazar.authorization.database.repository.RoleRepository
import org.bazar.authorization.database.repository.RolesActionsRepository
import org.bazar.authorization.database.repository.SpaceUserRepository
import org.bazar.authorization.database.tables.SpaceUsers
import org.bazar.authorization.utils.extensions.builder.buildRolesActions
import org.bazar.authorization.utils.extensions.builder.buildSpaceUser
import org.bazar.authorization.utils.extensions.mapper.toSpaceUserEntity
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

    fun createSpaceUser(spaceId: Long, userId: UUID, roleId: Long, isCreator: Boolean) = transaction {
        spaceUserRepository.save(buildSpaceUser(spaceId, userId, roleId, isCreator))
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

    fun createRole(spaceId: Long): Long = transaction {
        roleRepository.save(RoleEntity(RoleScope.SPACE, "rand", true, UUID.randomUUID(), spaceId)).id!!
    }

    fun getAllSpaceUsers(spaceId: Long): List<SpaceUserEntity> {
        return transaction {
            SpaceUsers.selectAll()
                .where { SpaceUsers.spaceId eq spaceId }
                .map { it.toSpaceUserEntity() }
        }
    }

}