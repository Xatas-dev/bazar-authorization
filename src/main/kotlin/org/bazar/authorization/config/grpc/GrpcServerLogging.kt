package org.bazar.authorization.config.grpc

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.springframework.core.annotation.Order
import org.springframework.grpc.server.GlobalServerInterceptor
import org.springframework.stereotype.Component

@Component
@GlobalServerInterceptor
@Order(Int.MIN_VALUE)
class GrpcServerLogging(
    private val log: KLogger = KotlinLogging.logger { }
) : ServerInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val method = call.methodDescriptor.fullMethodName
        val startTime = System.currentTimeMillis()

        val delegate = next.startCall(call, headers)

        return object : SimpleForwardingServerCallListener<ReqT>(delegate) {

            override fun onMessage(message: ReqT) {
                log.debug { "gRPC request [${method}]: $message" }
                super.onMessage(message)
            }

            override fun onComplete() {
                log.info { "gRPC call completed [${method}] in ${System.currentTimeMillis() - startTime} ms" }
                super.onComplete()
            }

            override fun onCancel() {
                log.warn { "gRPC call cancelled [${method}]" }
                super.onCancel()
            }
        }
    }

}