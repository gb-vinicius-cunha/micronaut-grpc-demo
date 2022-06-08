package br.com.vroc.demo.application.handlers

import br.com.vroc.demo.application.exceptions.ApiException
import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
@SuppressWarnings("TooGenericExceptionCaught")
class ExceptionHandlerGrpcServerInterceptor : ServerInterceptor {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    internal class ExceptionTranslatingServerCall<ReqT, RespT>(
        call: ServerCall<ReqT, RespT>,
        private val metadata: Metadata
    ) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {

        private var shouldProcessError = true

        override fun close(status: Status, trailers: Metadata) {
            if (!shouldProcessError) {
                return super.close(status, trailers)
            }

            val correlationId = getCorrelationId(metadata)

            var newStatus = status
            val cause = status.cause

            cause?.run {
                if (status.code == Status.Code.UNKNOWN) {
                    newStatus = when (cause) {
                        is ApiException -> cause.status.withDescription(cause.message)
                        else -> Status.INTERNAL.withDescription(cause.message)
                    }
                }

                val exception = status.cause
                logger.error("An error occurred in the request", exception)
            }

            if (!trailers.containsKey(correlationIdKey)) {
                trailers.put(correlationIdKey, correlationId)
            }

            shouldProcessError = false
            super.close(newStatus, trailers)
        }
    }

    private class ExceptionListener<ReqT>(
        delegate: ServerCall.Listener<ReqT>
    ) : SimpleForwardingServerCallListener<ReqT>(delegate)

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        metadata: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val exceptionTranslatingServerCall = ExceptionTranslatingServerCall(call, metadata)
        val listener = next.startCall(exceptionTranslatingServerCall, metadata)
        return ExceptionListener(listener)
    }
}
