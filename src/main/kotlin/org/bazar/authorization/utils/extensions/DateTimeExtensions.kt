package org.bazar.authorization.utils.extensions

import java.sql.Timestamp
import java.time.Instant

fun Instant.toTimestamp(): Timestamp = Timestamp.from(this)