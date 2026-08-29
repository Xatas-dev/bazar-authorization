package org.bazar.authorization.application.shared

import java.util.UUID

interface AuthenticatedUserProvider {

    suspend fun getToken(): String

    suspend fun getUserId(): UUID

}
