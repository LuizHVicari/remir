package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SaveSleepGoalRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** Exposed DSL only. Runs inside `dbQuery` (opened by the Service). Returns `core` DTOs. */
class SleepRepository {
    /** Newest first, capped so the response stays small until the app needs pagination. */
    fun list(
        userId: String,
        limit: Int = LIST_LIMIT,
    ): List<SleepRecordDto> =
        SleepRecordsTable
            .selectAll()
            .where { SleepRecordsTable.userId eq userId }
            .orderBy(SleepRecordsTable.bedtime, SortOrder.DESC)
            .limit(limit)
            .map { it.toDto() }

    fun insert(
        userId: String,
        request: CreateSleepRecordRequest,
        now: Instant,
    ): SleepRecordDto {
        val newId = Uuid.random()
        SleepRecordsTable.insert {
            it[id] = newId
            it[SleepRecordsTable.userId] = userId
            it[bedtime] = request.bedtime
            it[fellAsleepAt] = request.fellAsleepAt
            it[wokeUpAt] = request.wokeUpAt
            it[gotUpAt] = request.gotUpAt
            it[createdAt] = now
        }
        return SleepRecordDto(
            id = newId,
            bedtime = request.bedtime,
            fellAsleepAt = request.fellAsleepAt,
            wokeUpAt = request.wokeUpAt,
            gotUpAt = request.gotUpAt,
            createdAt = now,
        )
    }

    fun findGoal(userId: String): SleepGoalDto? =
        SleepGoalsTable
            .selectAll()
            .where { SleepGoalsTable.userId eq userId }
            .singleOrNull()
            ?.toGoalDto()

    /** Creates the goal or replaces it as a whole: a part left null is cleared. */
    fun saveGoal(
        userId: String,
        request: SaveSleepGoalRequest,
        now: Instant,
    ): SleepGoalDto {
        SleepGoalsTable.upsert {
            it[SleepGoalsTable.userId] = userId
            it[bedtime] = request.bedtime
            it[wakeUpTime] = request.wakeUpTime
            it[sleepMinutes] = request.sleepMinutes
            it[updatedAt] = now
        }
        return SleepGoalDto(request.bedtime, request.wakeUpTime, request.sleepMinutes, now)
    }

    private fun ResultRow.toGoalDto() =
        SleepGoalDto(
            bedtime = this[SleepGoalsTable.bedtime],
            wakeUpTime = this[SleepGoalsTable.wakeUpTime],
            sleepMinutes = this[SleepGoalsTable.sleepMinutes],
            updatedAt = this[SleepGoalsTable.updatedAt],
        )

    private fun ResultRow.toDto() =
        SleepRecordDto(
            id = this[SleepRecordsTable.id],
            bedtime = this[SleepRecordsTable.bedtime],
            fellAsleepAt = this[SleepRecordsTable.fellAsleepAt],
            wokeUpAt = this[SleepRecordsTable.wokeUpAt],
            gotUpAt = this[SleepRecordsTable.gotUpAt],
            createdAt = this[SleepRecordsTable.createdAt],
        )

    private companion object {
        const val LIST_LIMIT = 100
    }
}
