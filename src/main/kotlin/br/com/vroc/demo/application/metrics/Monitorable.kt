package br.com.vroc.demo.application.metrics

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import io.micronaut.core.annotation.Internal

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Around
@Type(MetricInterceptor::class)
@Internal
annotation class Monitorable
