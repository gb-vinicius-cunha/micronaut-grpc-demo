package br.com.vroc.demo.application.exceptions

import io.grpc.Status
import io.grpc.Status.ALREADY_EXISTS
import io.grpc.Status.INTERNAL
import io.grpc.Status.INVALID_ARGUMENT
import io.grpc.Status.NOT_FOUND

open class ApiException(
    message: String,
    val status: Status,
    throwable: Throwable? = null
) : RuntimeException(message, throwable)

class ValidationException(message: String) : ApiException(message, INVALID_ARGUMENT)

class ResourceAlreadyExistsException(message: String) : ApiException(message, ALREADY_EXISTS)

class ResourceNotFoundException(message: String) : ApiException(message, NOT_FOUND)

class RepositoryException(message: String) : ApiException(message, INTERNAL)
