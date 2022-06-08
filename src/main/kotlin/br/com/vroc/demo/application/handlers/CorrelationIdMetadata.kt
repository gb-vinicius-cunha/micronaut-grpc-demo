package br.com.vroc.demo.application.handlers

import io.grpc.Metadata
import java.util.UUID

val correlationIdKey: Metadata.Key<String> =
    Metadata.Key.of("correlation-id", Metadata.ASCII_STRING_MARSHALLER)

fun getCorrelationId(metadata: Metadata): String {
    val uuid = UUID.randomUUID().toString()

    val headerCorrelationId = metadata.get(correlationIdKey)
    return if (headerCorrelationId != null) {
        headerCorrelationId.ifBlank {
            metadata.put(correlationIdKey, uuid)
            uuid
        }
    } else {
        metadata.put(correlationIdKey, uuid)
        uuid
    }
}
