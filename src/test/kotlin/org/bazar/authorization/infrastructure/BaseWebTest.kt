package org.bazar.authorization.infrastructure

import io.ktor.server.testing.ApplicationTestBuilder

abstract class BaseWebTest : BaseIntegrationTest() {

    protected fun webTest(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = integrationTest {
        block.invoke(this)
    }

}