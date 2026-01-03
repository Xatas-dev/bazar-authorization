package org.bazar.authorization.config.grpc.interceptors

import io.grpc.*
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class KotlinServerInterceptor : ServerInterceptor {
    companion object {
        val SECURITY_CONTEXT_KEY: Context.Key<SecurityContext> =
            Context.key("spring-security-context")
    }

    override fun <ReqT, RespT> interceptCall(
        p0: ServerCall<ReqT, RespT>,
        p1: Metadata,
        p2: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        val context = SecurityContextHolder.getContext()
        val grpcContext = Context.current()
            .withValue(SECURITY_CONTEXT_KEY, context)

        return Contexts.interceptCall(grpcContext, p0, p1, p2)
    }
}

object KotlinSecurityContextHolder {

    fun getContext(): SecurityContext {
        return KotlinServerInterceptor.SECURITY_CONTEXT_KEY.get()
    }

}