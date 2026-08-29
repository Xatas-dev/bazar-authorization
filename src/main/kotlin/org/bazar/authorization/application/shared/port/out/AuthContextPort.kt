package org.bazar.authorization.application.shared.port.out

import java.util.UUID

interface AuthContextPort {

    suspend fun getToken(): String?

    suspend fun getUserId(): UUID?

}
