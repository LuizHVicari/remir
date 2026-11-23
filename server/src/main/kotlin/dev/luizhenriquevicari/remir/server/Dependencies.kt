package dev.luizhenriquevicari.remir.server

import com.auth0.jwk.JwkProvider
import com.zaxxer.hikari.HikariDataSource
import dev.luizhenriquevicari.remir.server.config.AppConfig
import dev.luizhenriquevicari.remir.server.db.connectDatabase
import dev.luizhenriquevicari.remir.server.features.legal.LegalRepository
import dev.luizhenriquevicari.remir.server.features.legal.LegalService
import dev.luizhenriquevicari.remir.server.features.profile.ProfileRepository
import dev.luizhenriquevicari.remir.server.features.profile.ProfileService
import dev.luizhenriquevicari.remir.server.features.sleep.SleepRepository
import dev.luizhenriquevicari.remir.server.features.sleep.SleepService
import dev.luizhenriquevicari.remir.server.plugins.clerkJwkProvider

/**
 * Manual constructor injection. For each new feature, instantiate its Repository and Service here.
 * No DI framework on the server.
 */
class Dependencies(
    val config: AppConfig,
    val jwkProvider: JwkProvider,
    private val dataSource: HikariDataSource,
) : AutoCloseable {
    private val profileRepository = ProfileRepository()

    val profileService = ProfileService(profileRepository)
    val legalService = LegalService(LegalRepository(), profileRepository)
    val sleepService = SleepService(SleepRepository(), profileRepository)

    override fun close() = dataSource.close()

    companion object {
        fun create(
            config: AppConfig,
            jwkProvider: JwkProvider = clerkJwkProvider(config.clerk.issuer),
        ) = Dependencies(config, jwkProvider, connectDatabase(config.database))
    }
}
