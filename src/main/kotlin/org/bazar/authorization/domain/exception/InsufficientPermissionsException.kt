package org.bazar.authorization.domain.exception

import java.util.UUID

class InsufficientPermissionsException(val userId: UUID, val spaceId: Long, val action: String, val resource: String) :
    DomainException("User $userId is not allowed to perform '$action' for resource $resource in space $spaceId")