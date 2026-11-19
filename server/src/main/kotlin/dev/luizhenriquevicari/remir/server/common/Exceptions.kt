package dev.luizhenriquevicari.remir.server.common

/**
 * Domain exceptions. `StatusPages` turns each one into an `ApiError` + HTTP status
 * (see `plugins/StatusPages.kt`). Never respond with an error straight from a route.
 */
sealed class DomainException(
    val code: String,
    message: String,
) : RuntimeException(message)

class NotFound(
    message: String = "Resource not found",
) : DomainException("not_found", message)

class Conflict(
    message: String,
) : DomainException("conflict", message)

class Forbidden(
    message: String = "Access denied",
) : DomainException("forbidden", message)

class ValidationFailed(
    val fields: Map<String, String>,
    message: String = "Invalid data",
) : DomainException("validation_failed", message)
