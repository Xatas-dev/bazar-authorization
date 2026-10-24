package org.bazar.authorization.application.action.port.`in`.impl

import org.bazar.authorization.application.action.port.`in`.GetActionsUseCase
import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.application.action.query.GetActionsQuery
import org.bazar.authorization.application.shared.buildAuthorizationCheck
import org.bazar.authorization.application.shared.port.out.Authorizer
import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.authz.AuthorizationAction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class GetActionsUseCaseImpl(
    private val actionRepositoryPort: ActionRepositoryPort,
    private val authorizer: Authorizer
) : GetActionsUseCase {

    override suspend fun execute(query: GetActionsQuery): List<Action> {
        authorizer.authorizeOrThrow(
            buildAuthorizationCheck(
                spaceId = query.spaceId,
                userId = query.requesterId,
                authorizationConstant = AuthorizationAction.ROLES_READ
            )
        )

        return suspendTransaction {
            actionRepositoryPort.findAll()
        }
    }
}
