package dev.luizhenriquevicari.remir.server.db

import dev.luizhenriquevicari.remir.server.support.ExposedTables
import dev.luizhenriquevicari.remir.server.support.TestDatabase
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import kotlin.test.Test
import kotlin.test.assertTrue

/** Fails when an Exposed `Table` changed but no migration was generated for it. */
class SchemaDriftTest {
    @Test
    fun `flyway migrations match the exposed tables`() =
        runTest {
            TestDatabase.dependencies // runs Flyway
            val tables = ExposedTables.all()
            assertTrue(tables.isNotEmpty(), "No Exposed tables found")

            val pending =
                dbQuery {
                    MigrationUtils.statementsRequiredForDatabaseMigration(
                        *tables.toTypedArray(),
                        withLogs = false,
                    )
                }

            assertTrue(
                pending.isEmpty(),
                "Schema drift; run ./gradlew :server:generateMigrations. Pending:\n${pending.joinToString("\n")}",
            )
        }
}
