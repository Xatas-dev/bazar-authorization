package org.bazar.authorization.adapter.inbound.rest

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import java.util.UUID

fun ApplicationCall.getAuthenticatedUserId(): UUID {
    val principal = this.principal<JWTPrincipal>()

    return principal?.payload?.subject?.let { UUID.fromString(it) }
        ?: throw BadRequestException("Can't obtain subject from jwt token")
}
