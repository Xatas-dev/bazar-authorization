package org.bazar.authorization.config

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory
import kotlin.collections.component1
import kotlin.collections.component2

fun applyLoggingLevels(appConfig: AppConfig) {
    val levels = appConfig.logging.level
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

    levels.forEach { (loggerName, levelStr) ->
        val logger = if (loggerName == "root") {
            loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
        } else {
            loggerContext.getLogger(loggerName)
        }

        logger.level = Level.toLevel(levelStr, Level.INFO)
    }
}