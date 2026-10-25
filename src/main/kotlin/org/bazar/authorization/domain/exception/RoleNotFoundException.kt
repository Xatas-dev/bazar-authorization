package org.bazar.authorization.domain.exception

class RoleNotFoundException(val roleId: Long) :
    DomainException("Role $roleId not found")