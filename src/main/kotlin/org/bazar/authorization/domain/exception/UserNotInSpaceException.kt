package org.bazar.authorization.domain.exception

import java.util.UUID

class UserNotInSpaceException(val userId: UUID, val spaceId: Long) :
    DomainException("User $userId is not a member of space $spaceId")