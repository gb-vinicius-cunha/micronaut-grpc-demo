package br.com.vroc.demo.application.endpoints

import assertk.assertThat
import assertk.assertions.isEqualTo
import br.com.vroc.demo.grpc.DemoGrpcRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@MicronautTest
class DemoEndpointTest {

    @Test
    fun `should return hello name`(endpoint: DemoEndpoint) {
        runBlocking {
            val name = "Shiryu"
            val request = DemoGrpcRequest.newBuilder().setName(name).build()

            val response = endpoint.send(request)

            assertThat(response.message).isEqualTo("Hello $name")
        }
    }
}
