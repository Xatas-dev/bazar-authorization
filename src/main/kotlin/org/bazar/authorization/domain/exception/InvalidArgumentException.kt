package org.bazar.authorization.domain.exception

class InvalidArgumentException(val field: String, val reason: String) :
    DomainException("Invalid '$field': $reason")
