package org.bazar.authorization.utils.extensions

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import java.util.*


fun ApplicationCall.getAuthenticatedUserId(): UUID {
    val principal = this.principal<JWTPrincipal>()

    return principal?.payload?.subject?.toUuid()
        ?: throw BadRequestException("Can't obtain subject from jwt token")
}