package org.bazar.authorization.utils

import io.ktor.util.logging.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> T.logger(): Logger =
    LoggerFactory.getLogger(T::class.java)