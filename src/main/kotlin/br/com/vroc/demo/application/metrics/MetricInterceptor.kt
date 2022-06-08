package br.com.vroc.demo.application.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit.MILLISECONDS

@Singleton
@Requires(bean = MeterRegistry::class)
class MetricInterceptor(
    private val meterRegistry: MeterRegistry
) : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        meterRegistry.counter("${context.methodName}.count").increment()
        val start = Instant.now()

        return try {
            context.proceed()
        } finally {
            meterRegistry.timer("${context.methodName}.timer").record(
                Duration.between(start, Instant.now()).toMillis(),
                MILLISECONDS
            )
        }
    }
}
