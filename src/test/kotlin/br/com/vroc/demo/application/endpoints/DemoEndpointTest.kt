package br.com.vroc.demo.application.endpoints

import assertk.assertThat
import assertk.assertions.isEqualTo
import br.com.vroc.demo.grpc.DemoGrpcRequest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class DemoEndpointTest {

    @Test
    fun `should return hello name`() {
        runBlocking {
            val endpoint = DemoEndpoint()

            val name = "Shiryu"
            val request = DemoGrpcRequest.newBuilder().setName(name).build()

            val response = endpoint.send(request)

            assertThat(response.message).isEqualTo("Hello $name")
        }
    }
}
