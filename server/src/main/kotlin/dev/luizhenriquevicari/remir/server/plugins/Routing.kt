package dev.luizhenriquevicari.remir.server.plugins

import dev.luizhenriquevicari.remir.server.Dependencies
import dev.luizhenriquevicari.remir.server.features.profile.profileRoutes
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(dependencies: Dependencies) {
    routing {
        get("/health") { call.respondText("ok") }

        // Every API route goes in here.
        authenticate(CLERK_AUTH) {
            profileRoutes(dependencies.profileService)
        }
    }
}
