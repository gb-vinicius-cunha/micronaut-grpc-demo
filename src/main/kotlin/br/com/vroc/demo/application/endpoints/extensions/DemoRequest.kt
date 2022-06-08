package br.com.vroc.demo.application.endpoints.extensions

import br.com.vroc.demo.domain.model.Demo
import br.com.vroc.demo.grpc.DemoGrpcRequest

fun DemoGrpcRequest.toDomain() =
    Demo(name)
