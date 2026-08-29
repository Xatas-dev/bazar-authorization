package org.bazar.authorization.infrastructure.plugins.security

import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class SecurityContext(
    val token: String,
    val userId: UUID?
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<SecurityContext>
}