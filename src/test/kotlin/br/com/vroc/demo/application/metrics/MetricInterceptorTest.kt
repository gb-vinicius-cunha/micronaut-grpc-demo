package br.com.vroc.demo.application.metrics

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micronaut.aop.MethodInvocationContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class MetricInterceptorTest {

    private val meterRegistry = mockk<MeterRegistry>(relaxed = true)
    private val context = mockk<MethodInvocationContext<Any, Any>>()
    private val metricInterceptor = MetricInterceptor(meterRegistry)
    private val counter = mockk<Counter>(relaxed = true)
    private val timer = mockk<Timer>(relaxed = true)

    @Test
    fun `should call the counter and timer when the flow does not throw an exception`() {
        val methodName = "test"

        mockkStatic(Instant::class)
        val start = Instant.now()
        val finish = start.plusMillis(500)
        every { Instant.now() } returnsMany listOf(start, finish)
        val expectedTime = Duration.between(start, finish).toMillis()

        val expectedReturn = "proceed"
        every { context.proceed() } returns expectedReturn

        every { context.methodName } returns methodName
        every { meterRegistry.counter("$methodName.count") } returns counter
        every { meterRegistry.timer("$methodName.timer") } returns timer

        val result = metricInterceptor.intercept(context)

        assertThat(result).isEqualTo(expectedReturn)

        verify { meterRegistry.counter("$methodName.count") }
        verify { counter.increment() }
        verify { meterRegistry.timer("$methodName.timer") }
        verify { timer.record(expectedTime, TimeUnit.MILLISECONDS) }
    }

    @Test
    fun `should call the counter and timer when the flow throws an exception`() {
        val methodName = "test"
        every { context.methodName } returns methodName
        every { meterRegistry.counter("$methodName.count") } returns counter
        every { meterRegistry.timer("$methodName.timer") } returns timer
        every { context.proceed() } throws Exception()

        assertThrows<Exception> {
            metricInterceptor.intercept(context)
        }

        verify { meterRegistry.counter("$methodName.count") }
        verify { counter.increment() }
        verify { meterRegistry.timer("$methodName.timer") }
        verify { timer.record(any(), TimeUnit.MILLISECONDS) }
    }
}
