package org.bazar.authorization.plugins

import io.ktor.server.application.*
import org.bazar.authorization.config.AppConfig
import org.bazar.authorization.config.applyLoggingLevels
import org.bazar.authorization.config.toAppConfig
import org.bazar.authorization.utils.Profile

fun Application.applyConfigurations(): AppConfig {
    val appConfig = environment.config.toAppConfig()
    applyLoggingLevels(appConfig)
    return appConfig
}


private fun getProfileFromEnv(): Profile {
    val profile = System.getenv("PROFILE")?.uppercase() ?: "PROD"
    return Profile.valueOf(profile)
}