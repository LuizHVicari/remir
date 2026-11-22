package dev.luizhenriquevicari.remir.server.features.sleep

import dev.luizhenriquevicari.remir.core.sleep.CreateSleepRecordRequest
import dev.luizhenriquevicari.remir.core.sleep.SleepRecordDto
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
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
