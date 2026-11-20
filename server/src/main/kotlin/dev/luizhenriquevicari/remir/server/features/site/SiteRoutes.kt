package dev.luizhenriquevicari.remir.server.features.site

import io.ktor.server.html.respondHtml
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/** Public: the home page that Google's consent screen and the stores point to. Mount it OUTSIDE `authenticate`. */
fun Route.siteRoutes() {
    get("/") { call.respondHtml { homePage() } }
}
