package org.bazar.authorization.grpc

import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.grpc.StatusException
import io.netty.handler.codec.http.HttpResponseStatus
import org.bazar.authorization.utils.exceptions.ApiException
import org.bazar.authorization.utils.logger

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

    private val logger = logger()

    fun handleException(exception: Throwable): StatusException {
        logger.error("Could not process gRPC request", exception)

        return when (exception) {
            is ApiException -> mapApiException(exception)
            else -> StatusException(Status.UNKNOWN.withDescription(exception.message))
        }
    }

    private fun mapApiException(exception: ApiException): StatusException {
        val status = when (exception.exceptionType.httpStatus) {
            HttpResponseStatus.UNAUTHORIZED -> Status.UNAUTHENTICATED
            HttpResponseStatus.FORBIDDEN -> Status.PERMISSION_DENIED
            HttpResponseStatus.BAD_REQUEST -> Status.INVALID_ARGUMENT
            HttpResponseStatus.NOT_FOUND -> Status.NOT_FOUND
            else -> Status.UNKNOWN
        }
        return StatusException(status.withDescription(exception.getFullErrorMessage()))
    }
}