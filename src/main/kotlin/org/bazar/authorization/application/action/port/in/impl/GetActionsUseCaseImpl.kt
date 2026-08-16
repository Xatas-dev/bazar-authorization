package org.bazar.authorization.application.action.port.`in`.impl

import org.bazar.authorization.application.action.port.`in`.GetActionsUseCase
import org.bazar.authorization.application.action.port.out.ActionAttributeRepositoryPort
import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.action.query.GetActionsQuery
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.application.action.ActionsWithAttributes
import org.bazar.authorization.domain.authz.Permission
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class GetActionsUseCaseImpl(
    private val actionRepositoryPort: ActionRepositoryPort,
    private val actionAttributeRepositoryPort: ActionAttributeRepositoryPort,
    private val authorizer: Authorizer
) : GetActionsUseCase {

    override suspend fun execute(query: GetActionsQuery): ActionsWithAttributes {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = query.spaceId,
                userId = query.requesterId,
                permission = Permission.ROLES_READ
            )
        )

        return suspendTransaction {
            val actions = actionRepositoryPort.findAll()
            val attributes = actionAttributeRepositoryPort.findAllByActionIds(actions.map { it.id })

            ActionsWithAttributes(actions, attributes)
        }
    }
}
