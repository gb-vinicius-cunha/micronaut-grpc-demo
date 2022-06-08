package br.com.vroc.demo.application.handlers

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import br.com.vroc.demo.application.exceptions.ApiException
import br.com.vroc.demo.application.handlers.ExceptionHandlerGrpcServerInterceptor.ExceptionTranslatingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.Status
import io.grpc.Status.ALREADY_EXISTS
import io.grpc.Status.INTERNAL
import io.grpc.Status.OK
import io.grpc.Status.UNKNOWN
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

internal class ExceptionHandlerTest {

    private val call = mockk<ServerCall<*, *>>(relaxed = true)

    private val uuid = UUID.randomUUID().toString()
    private val metadata = Metadata().apply {
        put(correlationIdKey, UUID.randomUUID().toString())
    }

    @Test
    fun `should process a request with success when the call is ok`() {
        val exceptionTranslatingServerCall = ExceptionTranslatingServerCall(call, metadata)
        val status = OK

        val trailers = Metadata()

        assertDoesNotThrow {
            exceptionTranslatingServerCall.close(status, trailers)
        }

        trailers.apply {
            put(correlationIdKey, uuid)
        }

        verify(exactly = 1) {
            call.close(status, trailers)
        }
    }

    @Test
    fun `should skip process when receive a second call`() {
        val exceptionTranslatingServerCall = ExceptionTranslatingServerCall(call, metadata)
        val status = OK

        val trailers = Metadata()

        exceptionTranslatingServerCall.close(status, trailers)
        exceptionTranslatingServerCall.close(status, trailers)

        verify(exactly = 2) { call.close(status, trailers) }
    }

    @Test
    fun `should process a request with success when the call is unknown and is an ApiException`() {
        val exceptionTranslatingServerCall = ExceptionTranslatingServerCall(call, metadata)
        val ex = ApiException("Resource already exists", ALREADY_EXISTS)

        val status = UNKNOWN.withCause(ex)
        val newStatus = ex.status.withDescription(ex.message)

        val slot = slot<Status>()
        every { call.close(capture(slot), any()) } just Runs

        val trailers = Metadata()

        exceptionTranslatingServerCall.close(status, trailers)

        verify(exactly = 1) { call.close(any(), any()) }

        assertThat(trailers.containsKey(correlationIdKey)).isTrue()
        assertThat(trailers[correlationIdKey]).isEqualTo(metadata[correlationIdKey])

        assertThat(slot.captured.code).isEqualTo(newStatus.code)
        assertThat(slot.captured.description).isEqualTo(newStatus.description)
    }

    @Test
    fun `should process a request with success when the call is unknown and is not an ApiException`() {
        val exceptionTranslatingServerCall = ExceptionTranslatingServerCall(call, metadata)
        val ex = RuntimeException("Runtime exception")

        val status = UNKNOWN.withCause(ex)
        val newStatus = INTERNAL.withDescription(ex.message)

        val slot = slot<Status>()
        every { call.close(capture(slot), any()) } just Runs

        val trailers = Metadata().apply {
            put(correlationIdKey, uuid)
        }

        exceptionTranslatingServerCall.close(status, trailers)

        verify(exactly = 1) { call.close(any(), any()) }

        assertThat(trailers.containsKey(correlationIdKey)).isTrue()
        assertThat(trailers[correlationIdKey]).isEqualTo(uuid)

        assertThat(slot.captured.code).isEqualTo(newStatus.code)
        assertThat(slot.captured.description).isEqualTo(newStatus.description)
    }

    @Test
    fun `should process a request with success when the call is not unknown`() {
        val exceptionTranslatingServerCall = ExceptionTranslatingServerCall(call, metadata)
        val status = ALREADY_EXISTS.withCause(ApiException("Resource already exists", ALREADY_EXISTS))

        val trailers = Metadata()

        assertDoesNotThrow {
            exceptionTranslatingServerCall.close(status, trailers)
        }

        trailers.apply {
            put(correlationIdKey, uuid)
        }

        verify(exactly = 1) {
            call.close(any(), trailers)
        }
    }
}
