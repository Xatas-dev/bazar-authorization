package org.bazar.authorization.infrastructure.util.extension

import java.util.UUID

fun String.toUuid(): UUID = UUID.fromString(this)
