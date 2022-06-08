package br.com.vroc.demo.application.handlers

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import io.grpc.Metadata
import org.junit.jupiter.api.Test

class CorrelationIdMetadataTest {

    @Test
    fun `should create correlationId if does not exists`() {
        val metadata = Metadata()

        val correlationId = getCorrelationId(metadata)

        assertThat(metadata.containsKey(correlationIdKey)).isTrue()
        assertThat(metadata[correlationIdKey]).isEqualTo(correlationId)
    }

    @Test
    fun `should create correlationId if is blank`() {
        val metadata = Metadata().apply {
            put(correlationIdKey, "")
        }

        val correlationId = getCorrelationId(metadata)

        assertThat(metadata[correlationIdKey]).isNotNull().isNotEmpty()
        assertThat(metadata[correlationIdKey]).isEqualTo(correlationId)
    }

    @Test
    fun `should keep correlationId if already exists`() {
        val expectedCorrelationId = "test"

        val metadata = Metadata().apply {
            put(correlationIdKey, expectedCorrelationId)
        }

        val correlationId = getCorrelationId(metadata)

        assertThat(correlationId).isEqualTo(expectedCorrelationId)
        assertThat(metadata[correlationIdKey]).isEqualTo(correlationId)
    }
}
