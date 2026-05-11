package org.bazar.authorization.utils.extensions

import java.util.*

fun String.toUuid(): UUID = UUID.fromString(this)