package org.bazar.authorization.domain.exception

class UnauthenticatedException :
    DomainException("Request is not authenticated")