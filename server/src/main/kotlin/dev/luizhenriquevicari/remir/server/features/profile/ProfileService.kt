package dev.luizhenriquevicari.remir.server.features.profile

import dev.luizhenriquevicari.remir.core.profile.ProfileDto
import dev.luizhenriquevicari.remir.core.profile.UpdateProfileRequest
import dev.luizhenriquevicari.remir.server.common.NotFound
import dev.luizhenriquevicari.remir.server.db.dbQuery
import kotlin.time.Clock

/** Business rules + transaction boundary. Receives requests already format-validated by the route. */
class ProfileService(
    private val repository: ProfileRepository,
    private val clock: Clock = Clock.System,
) {
    /** Provisions the user on first access and returns the profile. */
    suspend fun getOrProvision(userId: String): ProfileDto = dbQuery {
        repository.provision(userId, clock.now())
        repository.find(userId) ?: throw NotFound("User not found")
    }

    suspend fun update(userId: String, request: UpdateProfileRequest): ProfileDto = dbQuery {
        val now = clock.now()
        repository.provision(userId, now)
        repository.updateDisplayName(userId, request.displayName, now)
        repository.find(userId) ?: throw NotFound("User not found")
    }
}
