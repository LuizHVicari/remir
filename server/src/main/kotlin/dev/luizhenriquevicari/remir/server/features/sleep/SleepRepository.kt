package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SaveSleepGoalRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepGoalDto
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** Exposed DSL only. Runs inside `dbQuery` (opened by the Service). Returns `core` DTOs. */
class SleepRepository {
    /**
     * One page of the user's nights, newest first, optionally only those that started at or after [since]. The id
     * breaks ties between nights that started at the same moment, so pages never repeat or skip a night.
     */
    fun list(
        userId: String,
        since: Instant?,
        limit: Int,
        offset: Int,
    ): List<SleepRecordDto> =
        SleepRecordsTable
            .selectAll()
            .where { SleepRecordsTable.userId eq userId }
            .apply { since?.let { andWhere { SleepRecordsTable.bedtime greaterEq it } } }
            .orderBy(SleepRecordsTable.bedtime to SortOrder.DESC, SleepRecordsTable.id to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
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
        // Read the row back instead of building the DTO from `now`: Postgres keeps microseconds, the clock has
        // nanoseconds, and the response must equal what a later GET returns.
        return SleepRecordsTable
            .selectAll()
            .where { SleepRecordsTable.id eq newId }
            .single()
            .toDto()
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
        // Same reason as in `insert`: return what the database stored, not the in-memory clock value.
        return SleepGoalsTable
            .selectAll()
            .where { SleepGoalsTable.userId eq userId }
            .single()
            .toGoalDto()
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
}
