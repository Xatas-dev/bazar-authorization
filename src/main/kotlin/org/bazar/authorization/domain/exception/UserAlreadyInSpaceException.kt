package org.bazar.authorization.domain.exception

class UserAlreadyInSpaceException(val userId: Long, val spaceId: Long) :
    DomainException("User $userId is already a member of space $spaceId")