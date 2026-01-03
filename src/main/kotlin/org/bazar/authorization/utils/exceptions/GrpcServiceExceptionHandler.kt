package org.bazar.authorization.utils.exceptions

import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST
import io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN
import io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED
import org.springframework.grpc.server.exception.GrpcExceptionHandler
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.jwt.JwtValidationException
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {  }

@Component
class GrpcServiceExceptionHandler: GrpcExceptionHandler {

    override fun handleException(exception: Throwable): StatusException? {
        logger.error(exception) { "Could not process gRPC request" }
        return if (exception is ApiException) {
            when (exception.exceptionType.httpStatus) {
                UNAUTHORIZED -> StatusException(Status.UNAUTHENTICATED.withDescription(exception.getFullErrorMessage()))
                FORBIDDEN -> StatusException(Status.PERMISSION_DENIED.withDescription(exception.getFullErrorMessage()))
                BAD_REQUEST -> StatusException(Status.INVALID_ARGUMENT.withDescription(exception.getFullErrorMessage()))
                else -> StatusException(Status.UNKNOWN.withDescription(exception.getFullErrorMessage()))
            }
        }
        else
            when (exception) {
                is BadCredentialsException -> StatusException(Status.UNAUTHENTICATED.withDescription(exception.message))
                is InvalidBearerTokenException -> StatusException(Status.UNAUTHENTICATED.withDescription(exception.message))
                else -> StatusException(Status.UNKNOWN.withDescription(exception.message))
            }
    }

}