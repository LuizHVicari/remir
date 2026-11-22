package dev.luizhenriquevicari.remir.server.db

import dev.luizhenriquevicari.remir.server.features.profile.UsersTable
import dev.luizhenriquevicari.remir.server.support.TestDatabase
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** Instants must be stored as `timestamptz`, so the data does not depend on the zone the server runs in. */
class TimestampColumnsTest {
    @Test
    fun `no column is stored as a timestamp without a time zone`() =
        runTest {
            TestDatabase.dependencies // runs Flyway
            val naive =
                dbQuery {
                    exec(
                        """
                        SELECT table_name || '.' || column_name FROM information_schema.columns
                        WHERE table_schema = 'public' AND data_type = 'timestamp without time zone'
                          AND table_name <> 'flyway_schema_history'
                        """.trimIndent(),
                    ) { rs -> buildList { while (rs.next()) add(rs.getString(1)) } }
                }

            assertEquals(emptyList(), naive)
        }

    @Test
    fun `an instant is stored as the same moment whatever the JVM time zone is`() =
        runTest {
            TestDatabase.dependencies
            val instant = Instant.parse("2026-03-10T23:00:00Z")
            val userId = "user_test_${Uuid.random().toHexString()}"
            val original = TimeZone.getDefault()
            // Far from UTC, so a value written in the JVM's zone would be off by 14 hours.
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"))
            try {
                dbQuery {
                    UsersTable.insert {
                        it[id] = userId
                        it[createdAt] = instant
                        it[updatedAt] = instant
                    }
                }
                val storedEpoch =
                    dbQuery {
                        exec("SELECT extract(epoch FROM created_at)::bigint FROM users WHERE id = '$userId'") { rs ->
                            rs.next()
                            rs.getLong(1)
                        }
                    }
                val readBack = dbQuery { UsersTable.selectAll().where { UsersTable.id eq userId }.single() }

                assertEquals(instant.epochSeconds, storedEpoch)
                assertEquals(instant, readBack[UsersTable.createdAt])
            } finally {
                TimeZone.setDefault(original)
            }
        }
}
