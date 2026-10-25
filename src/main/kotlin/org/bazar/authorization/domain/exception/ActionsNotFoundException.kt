package org.bazar.authorization.domain.exception

class ActionsNotFoundException(val actionIds: Set<Long>) :
    DomainException("Actions not found: $actionIds")
