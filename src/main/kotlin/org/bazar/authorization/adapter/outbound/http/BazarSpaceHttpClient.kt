package org.bazar.authorization.adapter.outbound.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.bazar.authorization.application.shared.AuthenticatedUserProvider
import org.bazar.authorization.infrastructure.config.AppConfig
import org.bazar.authorization.infrastructure.util.logger

class BazarSpaceHttpClient(
    private val config: AppConfig,
    private val client: HttpClient,
    private val authenticatedUserProvider: AuthenticatedUserProvider
) {

    val logger = logger()

    suspend fun getSpaceUserInfo(spaceId: Long, userId: String): BazarSpaceUserResponse? {
        val response = client.get("${config.http.bazarSpaceUrl}/v1/spaces/$spaceId/users/$userId/raw") {
            bearerAuth(authenticatedUserProvider.getToken())
        }

        when (response.status) {
            HttpStatusCode.OK -> return response.body<BazarSpaceUserResponse>()
            HttpStatusCode.NotFound -> {
                logger.warn("No such user: $userId in space: $spaceId after calling bazar-space")
                return null
            }

            else -> {
                logger.warn("Unexpected error in space: ${response.status}")
                return null
            }
        }
    }

}