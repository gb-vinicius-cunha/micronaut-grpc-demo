package br.com.vroc.demo.application.endpoints

import br.com.vroc.demo.application.endpoints.extensions.toDomain
import br.com.vroc.demo.application.metrics.Monitorable
import br.com.vroc.demo.grpc.DemoGrpcReply
import br.com.vroc.demo.grpc.DemoGrpcRequest
import br.com.vroc.demo.grpc.DemoGrpcServiceGrpcKt.DemoGrpcServiceCoroutineImplBase
import io.micronaut.tracing.annotation.NewSpan
import jakarta.inject.Singleton

@Singleton
open class DemoEndpoint : DemoGrpcServiceCoroutineImplBase() {

    @Monitorable
    @NewSpan
    override suspend fun send(request: DemoGrpcRequest): DemoGrpcReply = with(request.toDomain()) {
        DemoGrpcReply.newBuilder().setMessage("Hello $name").build()
    }
}
