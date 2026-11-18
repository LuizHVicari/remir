package dev.luizhenriquevicari.remir.server.plugins

import dev.luizhenriquevicari.remir.core.api.ApiError
import dev.luizhenriquevicari.remir.server.common.Conflict
import dev.luizhenriquevicari.remir.server.common.DomainException
import dev.luizhenriquevicari.remir.server.common.Forbidden
import dev.luizhenriquevicari.remir.server.common.NotFound
import dev.luizhenriquevicari.remir.server.common.ValidationFailed
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

/** The only place that turns exceptions into an HTTP response + [ApiError]. */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<DomainException> { call, e ->
            val status = when (e) {
                is ValidationFailed -> HttpStatusCode.BadRequest
                is NotFound -> HttpStatusCode.NotFound
                is Conflict -> HttpStatusCode.Conflict
                is Forbidden -> HttpStatusCode.Forbidden
            }
            val fields = (e as? ValidationFailed)?.fields.orEmpty()
            call.respond(status, ApiError(e.code, e.message.orEmpty(), fields))
        }
        // Malformed JSON, invalid route parameter, etc.
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiError("bad_request", "Malformed request"))
        }
        exception<Throwable> { call, e ->
            call.application.environment.log.error("Unhandled error", e)
            call.respond(HttpStatusCode.InternalServerError, ApiError("internal_error", "Internal error"))
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ApiError("not_found", "Route not found"))
        }
    }
}
