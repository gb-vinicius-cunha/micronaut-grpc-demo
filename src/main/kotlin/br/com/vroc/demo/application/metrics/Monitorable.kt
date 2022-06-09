package br.com.vroc.demo.application.metrics

import io.micronaut.aop.Around
import io.micronaut.core.annotation.Internal

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Around
@Internal
annotation class Monitorable
