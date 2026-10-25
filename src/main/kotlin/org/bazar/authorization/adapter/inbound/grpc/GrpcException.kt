package org.bazar.authorization.adapter.inbound.grpc

import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.grpc.StatusException
import org.bazar.authorization.domain.exception.ActionsNotFoundException
import org.bazar.authorization.domain.exception.DomainException
import org.bazar.authorization.domain.exception.InsufficientPermissionsException
import org.bazar.authorization.domain.exception.InvalidArgumentException
import org.bazar.authorization.domain.exception.RoleNotFoundException
import org.bazar.authorization.domain.exception.UnauthenticatedException
import org.bazar.authorization.domain.exception.UnsatisfiedActionDependenciesException
import org.bazar.authorization.domain.exception.UserAlreadyInSpaceException
import org.bazar.authorization.domain.exception.UserNotInSpaceException
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
        val status = when (exception) {
            is UnauthenticatedException -> Status.UNAUTHENTICATED
            is InsufficientPermissionsException -> Status.PERMISSION_DENIED
            is UserNotInSpaceException -> Status.PERMISSION_DENIED
            is UserAlreadyInSpaceException -> Status.ALREADY_EXISTS
            is RoleNotFoundException -> Status.NOT_FOUND
            is ActionsNotFoundException -> Status.INVALID_ARGUMENT
            is InvalidArgumentException -> Status.INVALID_ARGUMENT
            is UnsatisfiedActionDependenciesException -> Status.FAILED_PRECONDITION
        }
        return StatusException(status.withDescription(exception.message))
    }
}
