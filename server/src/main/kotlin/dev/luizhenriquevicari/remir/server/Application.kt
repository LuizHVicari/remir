package dev.luizhenriquevicari.remir.server

import dev.luizhenriquevicari.remir.server.config.AppConfig
import dev.luizhenriquevicari.remir.server.plugins.configureAuth
import dev.luizhenriquevicari.remir.server.plugins.configureCallLogging
import dev.luizhenriquevicari.remir.server.plugins.configureRouting
import dev.luizhenriquevicari.remir.server.plugins.configureSerialization
import dev.luizhenriquevicari.remir.server.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) = EngineMain.main(args)

/** Entry point referenced by `application.conf`: builds the real dependencies and installs everything. */
fun Application.module() {
    val dependencies = Dependencies.create(AppConfig.from(environment.config))
    monitor.subscribe(ApplicationStopped) { dependencies.close() }
    setup(dependencies)
}

/** Installs plugins and routes. Separate from [module] so tests can inject dependencies. */
fun Application.setup(dependencies: Dependencies) {
    configureSerialization()
    configureCallLogging()
    configureStatusPages()
    configureAuth(dependencies.jwkProvider, dependencies.config.clerk.issuer)
    configureRouting(dependencies)
}
