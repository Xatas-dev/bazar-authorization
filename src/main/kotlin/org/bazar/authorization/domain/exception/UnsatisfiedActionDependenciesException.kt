package org.bazar.authorization.domain.exception

import org.bazar.authorization.domain.action.Action

class UnsatisfiedActionDependenciesException(
    val originalAction: Action,
    val missingDependencyAction: Action
) :
    DomainException(
        "Unsatisfied action dependency: " +
            "${originalAction.resource}:${originalAction.code}(id=${originalAction.id}) " +
                    "requires ${missingDependencyAction.resource}:${missingDependencyAction.code}(id=${missingDependencyAction.id})"

    )