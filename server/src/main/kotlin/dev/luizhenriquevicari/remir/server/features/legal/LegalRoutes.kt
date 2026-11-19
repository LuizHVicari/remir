package dev.luizhenriquevicari.remir.server.features.legal

import dev.luizhenriquevicari.remir.core.legal.AcceptLegalDocumentRequest
import dev.luizhenriquevicari.remir.core.legal.AdminLegalDocumentResource
import dev.luizhenriquevicari.remir.core.legal.LegalAcceptanceResource
import dev.luizhenriquevicari.remir.core.legal.LegalDocumentResource
import dev.luizhenriquevicari.remir.core.legal.LegalPageResource
import dev.luizhenriquevicari.remir.core.legal.LegalStatusDto
import dev.luizhenriquevicari.remir.core.legal.LegalValidation
import dev.luizhenriquevicari.remir.core.legal.MyLegalStatusResource
import dev.luizhenriquevicari.remir.core.legal.PublishLegalDocumentRequest
import dev.luizhenriquevicari.remir.server.common.orThrow
import dev.luizhenriquevicari.remir.server.common.requireAdmin
import dev.luizhenriquevicari.remir.server.common.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.html.respondHtml
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

/** Public: the page whose URL goes to Google and the stores. Mount it OUTSIDE `authenticate`. */
fun Route.legalPages(service: LegalService) {
    get<LegalPageResource> { resource ->
        // Fetched before respondHtml: its builder lambda is not a suspend scope.
        val document = service.current(resource.type)
        call.respondHtml { legalPage(document) }
    }
}

/** API for the app. Mount it inside `authenticate(CLERK_AUTH)`. */
fun Route.legalRoutes(service: LegalService) {
    get<MyLegalStatusResource> {
        call.respond(LegalStatusDto(service.pending(call.userId())))
    }

    get<LegalDocumentResource> { resource ->
        call.respond(service.current(resource.type))
    }

    post<LegalAcceptanceResource> { resource ->
        val request = LegalValidation.accept.orThrow(call.receive<AcceptLegalDocumentRequest>())
        service.accept(call.userId(), resource.type, request.version)
        call.respond(HttpStatusCode.NoContent)
    }

    post<AdminLegalDocumentResource> { resource ->
        call.requireAdmin()
        val request = LegalValidation.publish.orThrow(call.receive<PublishLegalDocumentRequest>())
        call.respond(HttpStatusCode.Created, service.publish(call.userId(), resource.type, request))
    }
}
