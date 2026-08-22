package org.bazar.authorization.adapter.inbound.grpc

import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.grpc.StatusException
import org.bazar.authorization.domain.exception.DomainErrors
import org.bazar.authorization.domain.exception.DomainException
import org.slf4j.LoggerFactory

class GrpcExceptionTranslatorInterceptor(private val grpcExceptionHandler: GrpcExceptionHandler) : ServerInterceptor {

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        val wrappedCall = object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            override fun close(status: Status, trailers: Metadata) {
                if (status.isOk) {
                    super.close(status, trailers)
                } else {
                    val exception = status.cause
                    if (exception != null) {
                        val statusException = grpcExceptionHandler.handleException(exception)
                        super.close(statusException.status, trailers)
                    } else {
                        super.close(status, trailers)
                    }
                }
            }
        }

        return try {
            next.startCall(wrappedCall, headers)
        } catch (e: Exception) {
            val statusException = grpcExceptionHandler.handleException(e)
            call.close(statusException.status, Metadata())
            object : ServerCall.Listener<ReqT>() {}
        }
    }
}

class GrpcExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun handleException(exception: Throwable): StatusException {
        logger.error("Could not process gRPC request", exception)

        return when (exception) {
            is DomainException -> mapDomainException(exception)
            else -> StatusException(Status.UNKNOWN.withDescription(exception.message))
        }
    }

    private fun mapDomainException(exception: DomainException): StatusException {
        val status = when (exception.exceptionType) {
            DomainErrors.ILLEGAL_ARGUMENT -> Status.INVALID_ARGUMENT
            DomainErrors.NO_SUCH_USER_IN_SPACE -> Status.PERMISSION_DENIED
            DomainErrors.NO_SUCH_ROLE -> Status.INVALID_ARGUMENT
            DomainErrors.INSUFFICIENT_PERMISSIONS -> Status.PERMISSION_DENIED
            DomainErrors.USER_ALREADY_EXISTS -> Status.ALREADY_EXISTS
            DomainErrors.NO_SUCH_ATTRIBUTE -> Status.INVALID_ARGUMENT
            DomainErrors.UNAUTHENTICATED -> Status.UNAUTHENTICATED
        }
        return StatusException(status.withDescription(exception.message))
    }
}
