package dev.luizhenriquevicari.remir.server.features.profile

import dev.luizhenriquevicari.remir.core.profile.MeResource
import dev.luizhenriquevicari.remir.core.profile.ProfileValidation
import dev.luizhenriquevicari.remir.core.profile.UpdateProfileRequest
import dev.luizhenriquevicari.remir.server.common.isAdmin
import dev.luizhenriquevicari.remir.server.common.orThrow
import dev.luizhenriquevicari.remir.server.common.userId
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

/** Plain HTTP: receive, validate (with the `core` validator), call the service, respond. */
fun Route.profileRoutes(service: ProfileService) {
    get<MeResource> {
        call.respond(service.getOrProvision(call.userId()).copy(isAdmin = call.isAdmin()))
    }

    put<MeResource> {
        val request = ProfileValidation.update.orThrow(call.receive<UpdateProfileRequest>())
        call.respond(service.update(call.userId(), request).copy(isAdmin = call.isAdmin()))
    }
}
