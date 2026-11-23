package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SaveSleepGoalRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import dev.luizhenriquevicari.remir.server.common.NotFound
import dev.luizhenriquevicari.remir.server.db.dbQuery
import dev.luizhenriquevicari.remir.server.features.profile.ProfileRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** Business rules + transaction boundary for sleep records. Requests arrive already validated by the route. */
class SleepService(
    private val repository: SleepRepository,
    private val profileRepository: ProfileRepository,
    private val clock: Clock = Clock.System,
) {
    /**
     * One page of the user's nights, newest first. Without a [limit] it is the latest [DEFAULT_LIMIT] nights, or up
     * to [MAX_LIMIT] when [since] narrows it (a bit over a year of nights, for the summary). A [limit] over
     * [MAX_LIMIT] or an [offset] below zero is quietly corrected, so a response always stays small.
     */
    suspend fun list(
        userId: String,
        since: Instant? = null,
        limit: Int? = null,
        offset: Int = 0,
    ): List<SleepRecordDto> =
        dbQuery {
            val wanted = limit ?: if (since == null) DEFAULT_LIMIT else MAX_LIMIT
            repository.list(userId, since, wanted.coerceIn(1, MAX_LIMIT), offset.coerceAtLeast(0))
        }

    suspend fun goal(userId: String): SleepGoalDto =
        dbQuery { repository.findGoal(userId) ?: throw NotFound("No sleep goal set") }

    suspend fun saveGoal(
        userId: String,
        request: SaveSleepGoalRequest,
    ): SleepGoalDto =
        dbQuery {
            val now = clock.now()
            profileRepository.provision(userId, now)
            repository.saveGoal(userId, request, now)
        }

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

    suspend fun get(
        userId: String,
        id: Uuid,
    ): SleepRecordDto = dbQuery { repository.find(userId, id) ?: throw NotFound(NIGHT_NOT_FOUND) }

    suspend fun update(
        userId: String,
        id: Uuid,
        request: CreateSleepRecordRequest,
    ): SleepRecordDto = dbQuery { repository.update(userId, id, request) ?: throw NotFound(NIGHT_NOT_FOUND) }

    suspend fun delete(
        userId: String,
        id: Uuid,
    ) = dbQuery { if (!repository.delete(userId, id)) throw NotFound(NIGHT_NOT_FOUND) }

    private companion object {
        const val NIGHT_NOT_FOUND = "Sleep record not found"
        const val DEFAULT_LIMIT = 100
        const val MAX_LIMIT = 400
    }
}
