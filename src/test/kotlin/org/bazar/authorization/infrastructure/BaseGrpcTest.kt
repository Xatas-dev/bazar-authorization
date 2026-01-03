package org.bazar.authorization.infrastructure

import org.bazar.authorization.grpc.AuthorizationAdminServiceGrpc.AuthorizationAdminServiceBlockingStub
import org.bazar.authorization.grpc.AuthorizationServiceGrpc
import org.bazar.authorization.grpc.AuthorizationServiceGrpc.AuthorizationServiceBlockingStub
import org.bazar.authorization.infrastructure.config.grpc.GrpcTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureInProcessTransport
import org.springframework.context.annotation.Import

@EnableAutoConfiguration
@AutoConfigureInProcessTransport
@Import(GrpcTestConfiguration::class)
abstract class BaseGrpcTest : BaseIntegrationTest() {

    @Autowired
    protected lateinit var adminStub: AuthorizationAdminServiceBlockingStub

    @Autowired
    protected lateinit var stub: AuthorizationServiceBlockingStub

}