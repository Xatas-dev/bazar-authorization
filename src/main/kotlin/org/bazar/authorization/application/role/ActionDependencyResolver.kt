package org.bazar.authorization.application.role

import org.bazar.authorization.application.action.port.out.ActionRepositoryPort
import org.bazar.authorization.domain.action.Action
import org.bazar.authorization.domain.exception.UnsatisfiedActionDependenciesException
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

class ActionDependencyResolver(
    private val actionRepositoryPort: ActionRepositoryPort
) {

    /**
     * Validate action dependencies with Action.dependsOn field.
     * @param actions - List of actions to check dependencies on
     * @throws org.bazar.authorization.domain.exception.DomainException
     * if there is at least one action without satisfied dependency in the list
     */
    suspend fun validateActionsDependency(actions: List<Action>) {
        val actionIdSet = actions.mapTo(HashSet()) { it.id }
        val allActionsMap = suspendTransaction {
            actionRepositoryPort.findAll().associateBy { it.id }
        }

        actions.forEach { action ->
            val dependencyId = action.dependsOn
            if (dependencyId != null && dependencyId !in actionIdSet) {
                val missing = allActionsMap[dependencyId]
                    ?: error("Corrupt data: action ${action.id} depends on non-existent action $dependencyId")
                throw UnsatisfiedActionDependenciesException(action, missing)
            }
        }
    }


}