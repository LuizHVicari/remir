package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import dev.luizhenriquevicari.remir.server.db.dbQuery
import dev.luizhenriquevicari.remir.server.features.profile.ProfileRepository
import kotlin.time.Clock

/** Business rules + transaction boundary for sleep records. Requests arrive already validated by the route. */
class SleepService(
    private val repository: SleepRepository,
    private val profileRepository: ProfileRepository,
    private val clock: Clock = Clock.System,
) {
    suspend fun list(userId: String): List<SleepRecordDto> = dbQuery { repository.list(userId) }

    suspend fun create(
        userId: String,
        request: CreateSleepRecordRequest,
    ): SleepRecordDto =
        dbQuery {
            val now = clock.now()
            // The foreign key needs the user row, which is otherwise created on the first /me call.
            profileRepository.provision(userId, now)
            repository.insert(userId, request, now)
        }
}
